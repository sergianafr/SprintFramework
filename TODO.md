## Sprint-0
### But:
    Afficher url
### Framework
- [x] Creation working dir
- [x] Creation projet test
- [x] Creation script pour transformer en jar
- [x] Update script deploiement projet test:
    - Ajout de jar du framework
- [x] Creation FronController
    - Creation servlet de frontController 
    - Recuperation url 
### Test
- [x] Configuration web.xml projet test
    - Creation balise servlet
    - Mapping vers '/'

## Sprint-1:
### But:
    Afficher les classes déclarées comme controller
    
#### Test:
- [x] creation+annotation Controllers test
- [x] Configuration web.xml;
    ajout package contenant les controllers dans init-param

#### Framework : 
- [x] Creation annotation pour controller
- [x] Ajout attributs dans FrontController
    - boolean checked
    - List<String> listControllers
- Dans processRequest:
    - [x] check si le l'attribut "checked" est true
        - si non
            - [x] scanner le contenu du package inseré dans web.xml client
            - [x] ajouter les noms des classes annotés dans listControllers
    - [x] Afficher le contenu de listControllers

## Sprint-2:
### But:
Get methodes dans les controllers

#### Framework:
- [x] Creation annotation pour les methodes: Get("url")
- [x] Creation classe mapping:
- [x] Dans init: 
    - [x] mettre dans init la recherche des controllers 
- [x] Recuperation des methodes qui possedent l'annotation get:
    - [x] Recuperation des controllers
    - [x] Creation hashmap{annotation value(url): Mapping}
- [x] map les methodes : HashMap {url: mapping}
- mapping={class: method}

## Sprint-3:
### But:
Invocation dynamique des methodes (afficher les string que retournent les methodes)

#### Framework:


## Sprint-4:
### But:
Invocation dynamique des methodes (afficher les string que retournent les methodes)

#### Framework:
Methode qui retourne un ModelView et redirection de page
[x] Creation classe ModelView:
    - String url
    - HashMap<> data;
[x] Redirection de la page vers l'url du model and view et affichage des data

## Sprint-5:
### But: 
Creation exception 

#### Framework:
[x] Exception de package vide
[x] Exception de package invalide
[x] Exception d'url dupliqué 
[x] Exception de type de retour qui n'est ni modelview ni string


## Sprint-6
### But: 
Recuperer des données d'un formulaire et l'envoyer dans la fonction a l'action (POST)

#### Framework:
- [x] Ajout d'attribut dans la classe Mapping: arguments de la fonction de type Parameter[]
- [x] Modification de la fonction invoke; il faut récuperer les arguments avant de l'invoquer
##### Partie 1:
- [x] Récuperation des paramètres dans la requete HTTP 
    - [x] Recherche des noms des arguments de la fonction
    - [x] Si les noms n'existent pas, on retourne null
    - [x] Recuperation des valeurs passés par la requete à partir des noms des arguments
##### Partie 2:
- [x] Creation annotation @param(name="")
- [x] Si le paramètre dans la requete HTTP au nom de l'argument n'existe pas, on check l'annotation @param:
    - [x] Si il existe: on recupere le name correspondant a l'annotation de l'argument
    - [x] Sinon, on retourne null
#### Test:
- [x] Creation méthode retournant ModelView qui prend en paramètre des arguments provenant d'un formulaire

## Sprint-7
### But:
Invoquer une methode ayant un object comme argument a partir d'un form

#### Framework:
- [x] Verification des parameters dans la methodes
    - [x] Si de type primitif: on recupere directement 
    - [x] Sinon, on recupere chaque attribut de l'objet et recupere un par un dans la requete

## Sprint-8
### But:
Créer des sessions a partir de controllers
#### Framework:
- [] Création classe CustomSession: 
    - [] attribut private HashMap<string, object> values
    - [] methodes:
        - [] public get()
        - [] public add()
        - [] public remove()
        - [] public update()
- [] Ajout conditions dans la vérification des méthodes dans les controllers:
    - [] Ajout Exception si un paramètre n'est pas annoté et pas de type CustomSession
- [] Creation methode HttpSessionToCustomSession: recupère les values du session et les store dans le values d'un CustomSession
- [] Creation methode CustomSessionToHttpSession: Transforme les valeurs du customSession en HttpSession

#### Test:
- [] Creation methode contenant CustomSession

