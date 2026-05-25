# Fluxo de Login — DDDStock

## 1. Visão Geral da Arquitetura

O login segue o padrão **MVVM** com três camadas:

```
LoginFragment (View)
    ↓  observa
AuthViewModel (ViewModel)
    ↓  chama
AuthRepository / FirestoreRepository / SessionManager (Model)
```

| Componente | Função |
|---|---|
| `LoginFragment` | UI do formulário de login (email + password + botão + loading) |
| `AuthViewModel` | Lógica de validação, autenticação, criação de sessão e estado |
| `AuthRepository` | Abstração sobre o Firebase Authentication |
| `FirestoreRepository` | Operações CRUD no Firestore (usuários, sessões, logs) |
| `SessionManager` | Persistência local via `SharedPreferences` |
| `AuthSession` | Modelo de dados da sessão armazenada no Firestore |
| `AppUser` | Modelo do perfil de usuário com campos de bloqueio |

A navegação entre fragmentos usa o **Jetpack Navigation** definido em `mobile_navigation.xml`.

---

## 2. Fluxo Passo a Passo

### 2.1 — UI: LoginFragment

O `LoginFragment` exibe o formulário com campos de email e password, botão "Sign In" e link "Create Account".

**Observadores de estado:**

```kotlin
viewModel.authState.observe(viewLifecycleOwner) { state ->
    when (state) {
        is AuthViewModel.AuthState.Loading -> btnLogin.showLoading()
        is AuthViewModel.AuthState.Success -> showSuccess()  // navega para Home
        is AuthViewModel.AuthState.Error   -> showError(state.message)
        is AuthViewModel.AuthState.Idle    -> btnLogin.hideLoading()
    }
}
```

**Ações do usuário:**
- Clicar **Sign In** → chama `viewModel.login(email, password)`
- Clicar **Create Account** → navega para `RegisterFragment` via `action_login_to_register`
- Digitar nos campos → esconde mensagens de erro anteriores

### 2.2 — Validação: AuthViewModel

Ao chamar `login()`, o ViewModel valida os campos antes de qualquer requisição:

| Campo | Validação | Erro típico |
|---|---|---|
| Email | RFC 5322 via `ValidationUtils.validateEmail()` | "Formato de email inválido" |
| Password | Mín. 7 caracteres via `ValidationUtils.validatePassword()` | "Senha muito curta" |

Se a validação falha, o estado muda para `AuthState.Error` e o fluxo para.

### 2.3 — Autenticação Firebase: AuthRepository

Com os campos válidos, inicia-se uma corrotina:

```kotlin
_authState.value = AuthState.Loading

val authResult = authRepo.loginWithEmail(email, password)
```

O `AuthRepository.loginWithEmail()` chama `FirebaseAuth.signInWithEmailAndPassword()`.

**Possíveis erros do Firebase:**

| Exceção | Mensagem |
|---|---|
| `FirebaseAuthInvalidUserException` | "Nenhuma conta encontrada com este email" |
| `FirebaseAuthInvalidCredentialsException` | "Email ou senha inválidos" |
| Outras | Mensagem genérica da exceção |

**Em caso de erro de credenciais**, o ViewModel registra um log de erro no Firestore:

```kotlin
logAuthError(type = AuthErrorLog.ErrorType.INVALID_PASSWORD, identifier = email)
```

O log contém: ID único, tipo do erro, identificador mascarado, timestamp e IP do dispositivo.

### 2.4 — Busca do Perfil: FirestoreRepository

Com a autenticação bem-sucedida, busca-se o documento do usuário no Firestore:

```kotlin
val userResult = firestoreRepo.getUserById(uid)
```

Coleção: `users/{uid}` → retorna um objeto `AppUser`.

Se o documento não existir, o login é recusado com "Perfil de usuário não encontrado".

### 2.5 — Verificação de Bloqueio: SecurityUtils

O sistema verifica se a conta está bloqueada:

```kotlin
if (SecurityUtils.isAccountLocked(user.failedAttempts, user.lockedUntil)) { ... }
```

**Critérios para conta bloqueada:**
- `lockedUntil` não é nulo
- `failedAttempts >= MAX_LOGIN_ATTEMPTS` (5 tentativas)
- O timestamp atual é anterior a `lockedUntil`

Se bloqueada, registra `ACCOUNT_LOCKED` no log de erros e exibe "Conta bloqueada. Tente novamente mais tarde."

### 2.6 — Reset de Tentativas e Atualização

Com a conta válida:

```kotlin
firestoreRepo.resetFailedAttempts(uid)   // failed_attempts = 0, locked_until = null
firestoreRepo.updateLastLogin(uid)       // last_login = now, updated_at = now
```

### 2.7 — Criação da Sessão

Uma sessão é criada e persistida em dois lugares:

**No Firestore** (`auth_sessions/{sessionId}`):
```kotlin
val session = AuthSession(
    sessionId = SecurityUtils.generateSessionId(),  // UUID v4
    uid = uid,
    createdAt = Timestamp.now(),
    expiresAt = Timestamp(now.seconds + 86400, 0),  // 24 horas
    ipAddress = SecurityUtils.getDeviceIpAddress(context)
)
firestoreRepo.createSession(session)
```

**Localmente** via `SessionManager` (SharedPreferences):
| Chave | Valor |
|---|---|
| `session_id` | UUID da sessão |
| `user_uid` | UID do Firebase |
| `session_expiry` | Timestamp de expiração (segundos) |
| `auth_state` | `true` |

### 2.8 — Navegação para Home

Com tudo concluído:

```kotlin
_authState.value = AuthState.Success(uid)
```

O `LoginFragment` observa o estado `Success` e navega:

```kotlin
findNavController().navigate(R.id.action_login_to_home)
```

---

## 3. Estados do AuthViewModel

```
Idle → Loading → Success(uid)
                → Error(message)
```

| Estado | Significado |
|---|---|
| `Idle` | Estado inicial, nenhuma operação em andamento |
| `Loading` | Requisição em andamento (botão com spinner) |
| `Success(uid)` | Login bem-sucedido, contém o UID do Firebase |
| `Error(message)` | Falha em qualquer etapa, contém mensagem legível |

---

## 4. Logout

O método `logout()` no ViewModel:
```kotlin
authRepo.signOut()       // FirebaseAuth.signOut()
sessionManager.clearSession()  // limpa SharedPreferences
_authState.value = AuthState.Idle
```

O `HomeFragment` ou botão de logout chama `viewModel.logout()` e navega para `loginFragment` via `action_global_logout`.

---

## 5. Sessão Persistente (Auto-login)

Ao abrir o app, `MainActivity.onCreate()` verifica:

```kotlin
if (sessionManager.isLoggedIn()) {
    navController.navigate(R.id.nav_home)
}
```

Isso pula a tela de login se o usuário já estiver autenticado.

---

## 6. Resumo das Camadas e Arquivos

| Arquivo | Caminho | Responsabilidade |
|---|---|---|
| `LoginFragment.kt` | `auth/LoginFragment.kt` | UI do formulário, animações, navegação |
| `AuthViewModel.kt` | `auth/AuthViewModel.kt` | Lógica de login, validação, sessão, estado |
| `AuthRepository.kt` | `firebase/AuthRepository.kt` | Chamadas ao Firebase Auth |
| `FirestoreRepository.kt` | `firebase/FirestoreRepository.kt` | CRUD no Firestore |
| `SessionManager.kt` | `service/SessionManager.kt` | SharedPreferences |
| `AuthSession.kt` | `model/AuthSession.kt` | Modelo da sessão |
| `AppUser.kt` | `model/AppUser.kt` | Modelo do usuário |
| `SecurityUtils.kt` | `util/SecurityUtils.kt` | Verificação de bloqueio, IP, UUID |
| `ValidationUtils.kt` | `util/ValidationUtils.kt` | Validação de email/password |
| `Constants.kt` | `util/Constants.kt` | Constantes (coleções, limites) |
| `MainActivity.kt` | `MainActivity.kt` | Auto-login, drawer, navegação global |
