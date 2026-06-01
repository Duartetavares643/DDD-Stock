# DDDStock — Guia do Fluxo de Autenticação

> Um guia simples e direto de como o login, registo e recuperação de senha funcionam no DDDStock.

---

## Índice

1. [Arquitetura (Visão Geral)](#1-arquitetura-visão-geral)
2. [Ecrãs (Telas) do App](#2-ecrãs-telas-do-app)
3. [Fluxo de Login](#3-fluxo-de-login)
4. [Fluxo de Registo](#4-fluxo-de-registo)
5. [Fluxo "Esqueceu a Senha"](#5-fluxo-esqueceu-a-senha)
6. [Auto-login (Sessão Persistente)](#6-auto-login-sessão-persistente)
7. [Logout](#7-logout)
8. [Mapa de Ficheiros](#8-mapa-de-ficheiros)

---

## 1. Arquitetura (Visão Geral)

O app segue o padrão **MVVM** em 3 camadas:

```
┌─────────────────────────────────────────────────┐
│                    TELA (UI)                    │
│   LoginFragment · RegisterFragment              │
│   ForgotPasswordFragment · HomeFragment         │
│                                                 │
│   O que o utilizador VÊ e com o que INTERAGE    │
└──────────────────────┬──────────────────────────┘
                       │  "observa" (LiveData)
                       ▼
┌─────────────────────────────────────────────────┐
│               VIEWMODEL (Lógica)                │
│   LoginViewModel · RegisterViewModel            │
│   ForgotPasswordViewModel · HomeViewModel       │
│                                                 │
│   Onde a VALIDAÇÃO e a LÓGICA de negócio vivem  │
└──────────────────────┬──────────────────────────┘
                       │  "chama" (suspend functions)
                       ▼
┌──────────────────────────────────────────────────┐
│               DATA (Dados)                       │
│   AuthRepository      → Firebase Authentication  │
│   FirestoreRepository → Firebase Firestore       │
│   SessionManager      → SharedPreferences (local)│
│                                                  │
│   Onde os DADOS são lidos/escritos               │
└──────────────────────────────────────────────────┘
```

### Regra de ouro (muito importante):

> **A Tela NUNCA toca na base de dados.**
> A Tela só fala com o ViewModel.
> O ViewModel é que fala com os repositórios.

---

## 2. Ecrãs (Telas) do App

O app tem 4 ecrãs principais. A navegação entre eles é feita com **Jetpack Navigation** (o sistema de navegação do Android).

```
┌──────────────┐       ┌──────────────────┐
│   LOGIN      │ ────> │   REGISTAR       │
│              │ <──── │                  │
│ (email +     │       │ (username, email,│
│  password)   │       │  password, PIN,  │
│              │       │  nome, contacto) │
│              │       └──────────────────┘
│              │
│     │        │       ┌──────────────────┐
│     └─────── │ ────> │ ESQUECEU SENHA   │
│              │       │                  │
│              │       │ (email para      │
│              │       │  redefinição)    │
│              │       └──────────────────┘
│              │
│     └─────── │ ────> ┌──────────────────┐
│                      │   HOME           │
│                      │                  │
│                      │ (dashboard após  │
│                      │  login)          │
│                      └──────────────────┘
└──────────────┘
```

---

## 3. Fluxo de Login

### 3.1 — O que o utilizador vê

Um ecrã com:
- Campo de **Email**
- Campo de **Password** (com olhinho para mostrar/ocultar)
- Link **"Forgot Password?"**
- Botão **"Sign In"** (que mostra um spinner a rodar enquanto carrega)
- Link **"Create Account"** (para novos utilizadores)
- Mensagem de erro vermelha se algo correr mal

### 3.2 — O que acontece quando o utilizador clica "Sign In"

```
UTILIZADOR clica "Sign In"
         │
         ▼
┌───────────────────────────────────────────────────────────┐
│  1. VALIDAÇÃO (LoginViewModel)                            │ 
│                                                           │
│  Email é válido?     < Se não >  "Email inválido"         │
│  Password ≥ 7 chars? < Se não >  "Password muito curta"   │
│                                                          │
│  Se tudo OK → continua                                    │
└───────────────────────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  2. AUTENTICAÇÃO (AuthRepository)   │
│                                     │
│  Chama Firebase Authentication      │
│  com email + password               │
│                                     │
│  Sucesso? → continua                │
│  Erro?    →  Mostra erro            │
│              ("Email ou password    │
│               inválidos")           │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  3. BUSCAR PERFIL (FirestoreRepos.) │
│                                     │
│  Vai ao Firestore buscar os dados   │
│  do utilizador (coleção "users")    │
│                                     │
│  Encontrou? → continua              │
│  Não?       →  "Perfil não          │
│                encontrado"          │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  4. CONTA BLOQUEADA?                │
│                                     │
│  O utilizador tem mais de 5         │
│  tentativas falhadas seguidas?      │
│                                     │
│  Sim? →  "Conta bloqueada.          │
│           Tente mais tarde."        │
│  Não? → continua                    │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  5. CRIAR SESSÃO                    │
│                                     │
│  Guarda no Firestore:               │
│  - ID da sessão (UUID)              │
│  - ID do utilizador                 │
│  - Data de criação                  │
│  - Data de expiração (24h)          │
│  - IP do dispositivo                │
│                                     │
│  Guarda no telemóvel (local):       │
│  - session_id                       │
│  - user_uid                         │
│  - session_expiry                   │
│  - auth_state = true                │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  6.  SUCESSO — Vai para a HOME      │
└─────────────────────────────────────┘
```

### 3.3 — Tratamento de erros

Sempre que o login falha, o sistema regista um **log de erro** no Firestore (coleção `auth_error_log`) com:
- ID único do erro
- Tipo de erro (password errada, conta bloqueada, etc.)
- Email do utilizador (parcialmente oculto: `j***@email.com`)
- Data e hora
- IP do dispositivo

Isto permite mais tarde auditoria de segurança.

---

## 4. Fluxo de Registo

### 4.1 — O que o utilizador vê

Um ecrã com:
- Campo **Username** (verifica automaticamente se já existe)
- Campo **First Name** (opcional)
- Campo **Surname** (opcional)
- Campo **Email** (verifica automaticamente se já existe)
- Campo **Contact** (opcional, formato telefone)
- Campo **Password** + indicador de força
- Campo **Security PIN** (4 dígitos)
- Botão **"Create Account"**
- Link **"Sign In"** (voltar)

### 4.2 — O que acontece quando o utilizador clica "Create Account"

```
UTILIZADOR clica "Create Account"
         │
         ▼
┌─────────────────────────────────────────┐
│  1. VALIDAÇÃO (RegisterViewModel)       │
│                                         │
│  Username: 3-50 caracteres, alfanumérico│
│  Email: formato válido                  │
│  Password: ≥7 chars, 1 letra + 1 número │
│  PIN: exatamente 4 dígitos, sem         │
│       sequências (1234, 1111)           │
│  Nome: letras e hífens (opcional)       │
│  Contacto: formato E.164 (opcional)     │
│                                         │
│  Se tudo OK → continua                  │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  2. VERIFICAR DUPLICADOS                │
│                                         │
│  Username já existe no Firestore?       │
│  Email já existe no Firestore?          │
│                                         │
│  Se não → continua                      │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  3. CRIAR CONTA (AuthRepository)        │
│                                         │
│  Firebase Auth cria conta com           │
│  email + password                       │
│                                         │
│  Obtém UID único do Firebase            │
│                                         │
│  Sucesso? → continua                    │
│  Erro?    →  Mostra erro                │
└─────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────┐
│  4. GUARDAR PERFIL (FirestoreRepos.)   │
│                                        │
│  Cria documento em "users/{uid}" com:  │
│  - uid, username, email                │
│  - firstName, surname, contact         │
│  - pin_hash, pin_salt (SHA-256)        │
│  - created_at, updated_at, last_login  │
│  - failed_attempts = 0                 │
│  - locked_until = null                 │
│                                        │
│  Se falhar → apaga conta Firebase      │
└────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  5.  SUCESSO — Vai para a HOME          |
└─────────────────────────────────────────┘
```

### 4.3 — Verificações em tempo real

Enquanto o utilizador preenche o formulário:
- **Username**: após 3 caracteres, verifica automaticamente se já existe (ícone verde/vermelho)
- **Email**: após escrever um email válido, verifica se já está registado
- **Password**: mostra a força da password (Fraca/Média/Forte/Muito Forte) com uma barra colorida

Isto tudo acontece **sem clicar em botão nenhum** — é automático.

---

## 5. Fluxo "Esqueceu a Senha"

### 5.1 — O que o utilizador vê

Um ecrã simples com:
- Título: "Reset Password"
- Subtítulo explicativo
- Campo de **Email**
- Botão **"Send Reset Link"**
- Mensagem de sucesso verde (quando o email é enviado)
- Link **"Back to Login"**

### 5.2 — O que acontece

```
UTILIZADOR clica "Forgot Password?" no Login
         │
         ▼
┌─────────────────────────────────────────┐
│  1. ABRE ecrã "Reset Password"          │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  2. UTILIZADOR escreve email            │
│     e clica "Send Reset Link"           │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  3. VALIDAÇÃO (ForgotPasswordViewModel) │
│                                         │
│  Email é válido? → continua             │
│  Inválido?       →  "Email inválido"    │
└─────────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────────┐
│  4. ENVIAR EMAIL (AuthRepository)       │
│                                         │
│  Firebase Auth envia email de           │
│  redefinição de password                │
│                                         │
│  Sucesso? →  "Reset link sent!"         │
│  Erro?    →  Mostra erro                │
└─────────────────────────────────────────┘
```

### 5.3 — Notas importantes

- O email de redefinição é enviado **pelo Firebase** — não precisamos de servidor próprio
- O email contém um link temporário para o utilizador definir uma nova password
- Após redefinir a password, o utilizador volta ao ecrã de Login e entra com a nova password
- O ecrã não navega automaticamente para lado nenhum — mostra mensagem de sucesso e o utilizador volta manualmente ao Login

---

## 6. Auto-login (Sessão Persistente)

### 6.1 — O que acontece quando o app abre

```
APP ABRE
    │
    ▼
┌─────────────────────────────────────────┐
│  MainActivity.onCreate()                │
│                                         │
│  Pergunta ao SessionManager:            │
│  "O utilizador já fez login antes?"     │
│                                         │
│  Sim? → Vai direto para a HOME          │
│         (pula o ecrã de Login)          │
│                                         │
│  Não? → Mostra o ecrã de Login          │
└─────────────────────────────────────────┘
```

### 6.2 — Como funciona a sessão local

O `SessionManager` guarda no telemóvel (SharedPreferences) 4 informações:

| O que guarda | Para que serve |
|---|---|
| `session_id` | ID único da sessão atual |
| `user_uid` | ID do utilizador no Firebase |
| `session_expiry` | Quando a sessão expira (timestamp) |
| `auth_state` | `true` se o utilizador está logado |

A sessão expira após **24 horas**. Depois disso, o utilizador tem de fazer login novamente.

---

## 7. Logout

### 7.1 — Como o utilizador faz logout

No ecrã **Home**, há um botão **"Logout"** (vermelho).

### 7.2 — O que acontece

```
UTILIZADOR clica "Logout"
         │
         ▼
┌─────────────────────────────────────────┐
│  1. LoginViewModel.logout()             │
│                                         │
│  a) FirebaseAuth.signOut()              │
│     (termina sessão no Firebase)        │
│                                         │
│  b) SessionManager.clearSession()       │
│     (apaga session_id, user_uid,        │
│      session_expiry do telemóvel)       │
│                                         │
│  c) Volta ao ecrã de Login              │
└─────────────────────────────────────────┘
```

---

## 8. Mapa de Ficheiros

Onde está cada peça do puzzle:

| O que é | Ficheiro | Caminho |
|---|---|---|
|  Tela de Login | `LoginFragment.kt` | `ui/auth/` |
|  Tela de Registo | `RegisterFragment.kt` | `ui/auth/` |
|  Tela Esqueceu Senha | `ForgotPasswordFragment.kt` | `ui/auth/` |
|  Tela Inicial | `HomeFragment.kt` | `ui/home/` |
|  Lógica de Login | `LoginViewModel.kt` | `ui/auth/` |
|  Lógica de Registo | `RegisterViewModel.kt` | `ui/auth/` |
|  Lógica Reset Senha | `ForgotPasswordViewModel.kt` | `ui/auth/` |
|  Lógica da Home | `HomeViewModel.kt` | `ui/home/` |
|  Firebase Auth | `AuthRepository.kt` | `data/` |
|  Firebase Firestore | `FirestoreRepository.kt` | `data/` |
|  Sessão local | `SessionManager.kt` | `data/` |
|  Modelo Utilizador | `AppUser.kt` | `data/` |
|  Modelo Sessão | `AuthSession.kt` | `data/` |
|  Modelo Erro | `AuthErrorLog.kt` | `data/` |
|  Validações | `ValidationUtils.kt` | `util/` |
|  Segurança (IP, bloqueio) | `SecurityUtils.kt` | `util/` |
|  PIN Hashing | `PinUtils.kt` | `util/` |
|  Constantes | `Constants.kt` | `util/` |
|  Atividade Principal | `MainActivity.kt` | `raiz` |
|  Navegação | `mobile_navigation.xml` | `res/navigation/` |

---

> **DDDStock** — Um app Android de gestão de stock com autenticação Firebase.
