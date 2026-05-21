# G4Api - Mini API REST (Java JEE)

API REST légère en Java JEE, sans framework lourd, avec sortie JSON.

Elle présente un exemple où on gère deux types d'objets et leurs relations :

| Objet  | Champs |
|--------|--------|
| `User` | `user_id`, `name`, `email` |
| `Task` | `task_id`, `title`, `description`, `creation_date`, `status` |

Les statuts de tâche (`status`) sont des entiers : `1` Backlog · `2` Todo · `3` In Progress · `4` Done · `5` Closed.

### Endpoints

| Méthode | URI | Description |
|---------|-----|-------------|
| `GET` | `/user/{id}` | Données d'un utilisateur |
| `GET` | `/user/{id}/task` | Liste des tâches d'un utilisateur |
| `POST` | `/task` | Créer une nouvelle tâche |
| `POST` / `PUT` | `/task/{id}` | Modifier une tâche existante |
| `DELETE` | `/task/{id}` | Supprimer une tâche |
| `POST` / `PUT` | `/task/user/{userId}/task/{taskId}` | Associer une tâche à un utilisateur |
| `DELETE` | `/task/user/{userId}/task/{taskId}` | Retirer l'association tâche ↔ utilisateur |

> L'API est conçue pour évoluer : nouveaux attributs et nouveaux endpoints peuvent être ajoutés sans rupture.

---

## Stack technique

- **Java 21** (LTS)
- **Jersey 3.x** (JAX-RS) — routing REST
- **Jetty embedded** — serveur léger
- **Jackson** — sérialisation JSON
- **HikariCP** — connection pooling
- **MySQL 8.0** — base de données
- **JUnit 5** — tests
- **Spotless** — formateur (Google Java Format AOSP)
- **Checkstyle** — linter

---

## Configuration

### Base de données

Les paramètres de connexion se configurent via des variables d'environnement :

```bash
DB_USER=root
DB_PWD=
DB_URL=jdbc:mysql://localhost:3306/rest_api?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Europe/Paris
```

### Port du serveur

```bash
PORT=8080
```

---

## Développement

### Prérequis

- Java 21+
- Maven 3.9+
- MySQL 8.0+ (ou Docker)

### Installation

```bash
mvn clean install
```

### Lancer avec Docker

```bash
docker compose up -d
```

L'API est accessible sur `http://localhost:8080`.

### Lancer localement

```bash
# Assurez-vous que MySQL tourne et que la base est initialisée
mysql -u root -p < share/sql/rest_api.sql

# Lancez l'API
mvn clean package
java -jar target/rest-api-java-1.0.0.jar
```

---

## Qualité du code

### Linter

```bash
mvn checkstyle:check
```

### Formatter

```bash
# Vérifier
mvn spotless:check

# Appliquer les corrections
mvn spotless:apply
```

### Tests

Les tests nécessitent une connexion base de données valide.

```bash
mvn test
```

Ou avec une base dédiée :

```bash
DB_USER=root DB_PWD=root DB_URL=jdbc:mysql://localhost:3306/rest_api mvn test
```

---

## CI/CD

Le pipeline GitHub Actions (`/.github/workflows/ci.yml`) exécute :

1. **Lint** — Checkstyle
2. **Format** — Spotless check
3. **Tests** — JUnit 5 avec MySQL service container
4. **Build** — fat jar avec Maven Shade
5. **Quality Gate** — vérifie que tous les jobs passent
6. **Auto-fix** — applique Spotless automatiquement sur les PR si le format échoue

---

## Format de réponse

L'API répond en `application/json` par défaut.

**Codes de réponse :**
- `200 OK` — Succès (retourne `1` pour les opérations simples)
- `201 Created` — Création de ressource (retourne l'objet complet)
- `400 Bad Request` — Paramètres invalides
- `404 Not Found` — Ressource introuvable
- `500 Internal Server Error` — Erreur interne

---

## Structure du projet

```
rest_api_java/
├── pom.xml                          # Maven, dépendances, plugins
├── Dockerfile                       # Image légère (eclipse-temurin:21-jre-alpine)
├── docker-compose.yml               # API + MySQL
├── checkstyle.xml                   # Configuration du linter
├── .github/workflows/ci.yml         # Pipeline CI/CD
├── share/sql/rest_api.sql           # Schéma de la base de données
└── src/
    ├── main/java/com/g4/api/
    │   ├── Main.java                # Point d'entrée (Jetty embedded)
    │   ├── ApiApplication.java      # Configuration JAX-RS
    │   ├── db/
    │   │   ├── DB.java              # Multiton HikariCP
    │   │   └── DBConfig.java        # Configuration via env vars
    │   ├── controller/
    │   │   ├── UserController.java  # @Path("/user")
    │   │   └── TaskController.java  # @Path("/task")
    │   ├── model/
    │   │   ├── ModelAbstract.java   # Classe de base
    │   │   ├── User.java            # Entité utilisateur
    │   │   ├── Task.java            # Entité tâche
    │   │   ├── UserTask.java        # Relation N:N
    │   │   └── TaskStatus.java      # Enum des statuts
    │   └── exception/
    │       └── ApiExceptionMapper.java  # Gestion d'erreurs
    └── test/java/com/g4/api/
        └── ApiTest.java             # Tests d'intégration
```
