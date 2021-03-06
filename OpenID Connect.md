OpenID Connect
==============

User Attribute Mapping
----------------------

### Google

-   id
-	email
-	emailIsVerified
-	name (full name)
-	firstName
-	lastName
-	picture (URL)
-	gender ("male", "female", ?)
-	locale (e.g., de-DE)
-	link (Google+ profile)

### Facebook

-   id
-	email
-	name (full name)
-	firstName
-	lastName
-	gender ("male", "female", ?)
-	link (Facebook profile)
-   locale
-   timezone
-   verified
-   updateTime

### Github
-   id         string
-	email      string    email
-   name       string    full name
-   link       string    github profile
-   picture    URL
-   bio        string
-   blog       string    URL
-   company    string
-   login      string
-   gravatarId null
-   location   string

### Union of Google, Facebook, Github

-   id         string
-	email      string    email
-   name       string    full name
-	firstName  string
-	lastName   string
-   link       string    github profile
-   picture    URL
-   bio        string
-   blog       string    URL
-   company    string
-   login      string
-	gender     string
-   gravatarId string    email
-   location   string
-   locale     locale
-   timezone   timezone
-   updateTime datetime
-   verified   boolean