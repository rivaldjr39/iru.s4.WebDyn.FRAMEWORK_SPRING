# Structure du projet

```text
iru.s4.WebDyn.FRAMEWORK_SPRING/
├── src/
│  └── main/
│       └── java/
│           └── mg/itu/rivaldo/
│               ├── annotation/
│               │   ├── Mapfile.java
│               │   ├── RestApi.java
│               │   ├── Session.java
│               │   ├── Url.java
│               │   └── UrlMethod.java
│               ├── util/
│               │   ├── BindingUtil.java
│               │   └── Util.java
│               └── controller/
│                   ├── FrontControllerServlet.java
│                   ├── Listner.java
│                   ├── Mapping.java
│                   ├── ModelAndVue.java
│                   ├── UrlType.java
│                   └── (autres classes de contrôle)
│   
│       
│           
├── lib/
│   └── dépendances .jar
├── build/
│   └── mg/itu/rivaldo/       # classes compilées, générées
├── framework.jar              # archive générée
├── run.sh                     # compilation sous Linux/macOS
├── run.bat                    # compilation sous Windows
├── README.md
└── structure.md
```

## Packages Java

Le chemin des fichiers correspond directement à leur déclaration `package` :

| Package | Rôle |
|---|---|
| `mg.itu.rivaldo.annotation` | Annotations et objets associés utilisés par les contrôleurs |
| `mg.itu.rivaldo.util` | Outils techniques partagés (binding, scan de classes, etc.) |
| `mg.itu.rivaldo.controller` | Servlet frontale et classes de mapping/contrôle HTTP |

Le déplacement vers `src/main/java` ne change donc pas les noms de packages.
Le contenu du JAR reste également sous `mg/itu/rivaldo/...`, ce qui conserve la
compatibilité avec l'application de test.

## Compiler et préparer l'application de test

Depuis la racine du projet :

```bash
./run.sh
```

Les scripts suppriment puis recréent `build/`, compilent tous les fichiers de
`src/main/java` dans `build/`, puis produisent `framework.jar`.

Pour utiliser le framework dans l'application de test, placer
`framework.jar` et les dépendances de `lib/` dans le classpath. Les contrôleurs
de l'application de test doivent conserver leurs propres packages et être
déclarés via le paramètre de scan attendu par le framework (`packageNames`).
