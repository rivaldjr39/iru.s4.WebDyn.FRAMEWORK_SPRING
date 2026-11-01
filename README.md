# itu.s4.WebDyn.Framework

Framework Java web basé sur Jakarta Servlet, avec intégration de Spring
`ApplicationContext`, routage par annotations, binding automatique des
paramètres HTTP, gestion de session, authentification, upload de fichiers et
réponses HTML/JSON.

## Architecture générale

```text
Client HTTP
    |
    v
FrontControllerServlet
    |
    +--> UrlType : identifie URL + verbe HTTP
    |
    +--> Mapping : associe une route à une classe et une méthode
    |
    +--> SingletonUtil : crée/réutilise l'instance du contrôleur
    |
    +--> AuthUtil : vérifie authentification et rôle
    |
    +--> BindingUtil : construit les objets à partir des paramètres HTTP
    |       |
    |       +--> Mapfile : représente les fichiers uploadés
    |
    +--> SessionUtil : convertit HttpSession <-> MapSession
    |
    +--> Exécution de la méthode du contrôleur
            |
            +--> @RestApi       : réponse JSON
            +--> ModelAndVue    : transfert vers une vue
            +--> autre valeur   : réponse texte

Initialisation du contexte
    |
    v
Listner
    |
    +--> Util : recherche les classes @Url et les méthodes @UrlMethod
    +--> MessageUtil : configure la langue des messages
    +--> ServletContext : stocke mappings, contrôleurs et configuration
```

## Structure du projet

```text
iru.s4.WebDyn.FRAMEWORK_SPRING/
├── src/main/java/mg/itu/rivaldo/
│   ├── annotation/
│   │   ├── Auth.java
│   │   ├── MapSession.java
│   │   ├── Mapfile.java
│   │   ├── RestApi.java
│   │   ├── Url.java
│   │   └── UrlMethod.java
│   ├── controller/
│   │   ├── FrontControllerServlet.java
│   │   ├── Listner.java
│   │   ├── Mapping.java
│   │   ├── ModelAndVue.java
│   │   └── UrlType.java
│   └── util/
│       ├── AuthUtil.java
│       ├── BindingUtil.java
│       ├── MessageUtil.java
│       ├── SessionUtil.java
│       ├── SingletonUtil.java
│       └── Util.java
├── lib/                  # Dépendances Jakarta, Spring et Jackson
├── build/                # Classes compilées
├── framework.jar         # Archive générée
├── run.sh                # Compilation et génération du JAR
├── structure.md
└── README.md
```

## Composants et responsabilités

### Package `mg.itu.rivaldo.annotation`

| Classe / annotation | Rôle |
|---|---|
| `@Url` | Marque une classe comme contrôleur détectable par le framework. |
| `@UrlMethod` | Déclare l'URL et le verbe HTTP d'une méthode. |
| `@RestApi` | Demande une réponse JSON pour la méthode exécutée. |
| `@Auth` | Protège une méthode et peut imposer un ou plusieurs rôles. |
| `MapSession` | Encapsule les attributs de session dans une `Map`. |
| `Mapfile` | Représente un fichier uploadé : nom, extension et contenu binaire. |

`@Url`, `@UrlMethod`, `@RestApi` et `@Auth` ont une rétention
`RUNTIME`, car elles sont lues par réflexion pendant l'exécution.

### Package `mg.itu.rivaldo.controller`

| Classe | Rôle |
|---|---|
| `Listner` | Initialise le framework au démarrage du contexte Servlet. |
| `FrontControllerServlet` | Reçoit les requêtes `/`, recherche la route et exécute la méthode cible. |
| `Mapping` | Contient la classe du contrôleur et la méthode Java associée. |
| `UrlType` | Clé de routage composée de l'URL et du verbe HTTP. |
| `ModelAndVue` | Transporte le nom de la vue et les données à placer dans la requête. |

### Package `mg.itu.rivaldo.util`

| Classe | Rôle |
|---|---|
| `Util` | Scanne le package configuré, détecte les classes `@Url` et crée les mappings `@UrlMethod`. |
| `SingletonUtil` | Maintient une instance unique de chaque contrôleur dans le `ServletContext`. |
| `BindingUtil` | Convertit les paramètres HTTP en objets Java, y compris les objets imbriqués et les fichiers. |
| `SessionUtil` | Synchronise `HttpSession` et `MapSession`. |
| `AuthUtil` | Vérifie la présence de l'utilisateur en session et ses rôles. |
| `MessageUtil` | Fournit les messages localisés en français ou en anglais. |

## Relations entre les classes

```text
Listner
 ├── utilise Util
 ├── utilise MessageUtil
 └── place dans ServletContext :
       mappingUrls : Map<UrlType, Mapping>
       controllerClassNames : List<String>
       prefixe, suffixe, lang, springContext

Util
 ├── détecte @Url
 ├── lit @UrlMethod
 ├── crée UrlType
 └── crée Mapping

FrontControllerServlet
 ├── lit les données préparées par Listner
 ├── utilise SingletonUtil
 ├── utilise AuthUtil et @Auth
 ├── utilise BindingUtil
 ├── utilise SessionUtil et MapSession
 ├── lit @RestApi
 └── retourne ModelAndVue ou JSON

BindingUtil
 └── construit Mapfile pour les champs de fichier

AuthUtil
 └── utilise MessageUtil pour les messages d'accès
```

## Cycle de démarrage

1. Le conteneur Servlet appelle `Listner.contextInitialized`.
2. `Listner` lit les paramètres `packageNames`, `prefixe`, `suffixe` et `lang`.
3. `Util` parcourt le package configuré.
4. Chaque classe annotée `@Url` est enregistrée.
5. Chaque méthode annotée `@UrlMethod` devient une entrée
   `UrlType -> Mapping`.
6. Les mappings et la configuration sont placés dans le `ServletContext`.
7. `FrontControllerServlet.init()` récupère cette configuration.

## Cycle d'une requête

1. `FrontControllerServlet` reçoit les requêtes `GET` ou `POST`.
2. Il construit une clé `UrlType` à partir du chemin et du verbe HTTP.
3. Il récupère le `Mapping` correspondant.
4. `SingletonUtil` récupère ou crée l'instance du contrôleur.
5. Si nécessaire, `AuthUtil` vérifie l'authentification et le rôle.
6. Les paramètres sont injectés selon leur type :
   - `String`, `int`, `double` : paramètres HTTP simples ;
   - `ApplicationContext` : contexte Spring ;
   - `MapSession` : session convertie par `SessionUtil` ;
   - autre objet : construction automatique par `BindingUtil`.
7. La méthode du contrôleur est appelée par réflexion.
8. Le résultat est traité :
   - `@RestApi` : sérialisation JSON avec Jackson ;
   - `ModelAndVue` : ajout des attributs puis `forward` vers la vue ;
   - autre résultat : affichage texte.

## Configuration attendue

Les paramètres suivants sont lus dans le contexte Servlet :

| Paramètre | Utilisation |
|---|---|
| `packageNames` | Package racine à scanner pour les contrôleurs. |
| `prefixe` | Préfixe du chemin des vues. |
| `suffixe` | Suffixe du chemin des vues, par exemple `.jsp`. |
| `lang` | Langue des messages : `fr` ou `en`. |
| `auth-session` | Nom de l'attribut de session indiquant l'utilisateur connecté. |
| `roles-session` | Nom de l'attribut de session contenant le rôle utilisateur. |
| `uploads.config` | Chemin vers `uploads.properties`, utilisé pour les fichiers. |

Le contexte Spring est récupéré par `Listner` via
`WebApplicationContextUtils` et peut être injecté dans une méthode de
contrôleur avec un paramètre de type `ApplicationContext`.

## Exemple de contrôleur utilisateur

```java
@Url
public class UserController {

    @UrlMethod(value = "/users", type = "GET")
    @RestApi
    public List<User> list() {
        return service.findAll();
    }

    @UrlMethod(value = "/admin", type = "GET")
    @Auth(role = "ADMIN")
    public ModelAndVue admin() {
        ModelAndVue result = new ModelAndVue("admin/index", new HashMap<>());
        result.setAttribute("title", "Administration");
        return result;
    }
}
```

Une route est donc identifiée par le couple URL/verbe, par exemple
`("/users", "GET")`. Deux méthodes ne peuvent pas utiliser le même couple :
`Util` signale alors un mapping dupliqué.