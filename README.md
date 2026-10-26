# Fil — l'actualité, simplement

Fil est une application Android minimaliste pour lire les actualités sans publicité, sans compte et sans tracking.

Le projet est open source et le code appartient à son auteur, Wensing Mathis.

## Présentation

Fil regroupe les articles issus de flux RSS/Atom choisis par l'utilisateur, avec une lecture claire et rapide.

- Sans publicité
- Sans compte utilisateur
- Sans suivi analytique
- Sans dépendance à un serveur propriétaire
- Compatible avec les flux RSS/Atom en HTTPS

## Fonctionnalités

- Lecture des titres, dates et résumés
- Ajout de sources comme Franceinfo, Le Monde et Service-Public
- Ajout de n'importe quel flux RSS/Atom
- Actualisation manuelle
- Sauvegarde locale des derniers articles consultés
- Mode clair et sombre
- Ouverture des articles complets dans le navigateur

## Android pris en charge

- Android 8.0 ou supérieur (API 26+)

## Build

### Avec Android Studio

1. Ouvrir le dossier `Fil-Actualites` dans Android Studio.
2. Récupérer les dépendances Gradle et le SDK Android.
3. Utiliser Java 17 et Gradle 8.9.
4. Exécuter :

```bash
gradle --no-daemon :app:testDebugUnitTest :app:assembleDebug
```

5. Récupérer le fichier :

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Via GitHub Actions

Le dépôt contient un workflow Android qui construit automatiquement l'APK à chaque push.

## Confidentialité

- Permission `INTERNET` uniquement
- Les flux sont téléchargés directement depuis leurs sources
- Aucune donnée de localisation, mot de passe ou identifiant publicitaire n'est demandée
- Les paramètres et les derniers articles sont enregistrés localement sur le téléphone

## Tech

- Java 17
- Android native
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- compileSdk 35
- minSdk 26
- RSS 2.0 / Atom
- Parsing XML via `XmlPullParser`

## Licence

Le code du projet est open source et publié sous licence MIT.

Le code appartient à son auteur, Wensing Mathis.

## Contribuer

Les contributions sont les bienvenues, à condition de respecter le cadre du projet et de conserver l'esprit open source et respectueux de la vie privée.
