## Sprint-0
### But:
    Afficher url

- [x] Creation working dir
- [x] Creation projet test
- [x] Creation script pour transformer en jar
- [x] Update script deploiement projet test:
    - Ajout de jar du framework
- [x] Creation FronController
    - Creation servlet de frontController 
    - Recuperation url 
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
- [] Creation annotation pour les methodes: Get("url")
- [ ] Creation classe mapping:
- [] Dans init: 
    - [] mettre dans init la recherche des controllers 
- [] Recuperation des methodes qui possedent l'annotation get:
    - [] Recuperation des controllers
    - [] Creation hashmap{annotation value(url): Mapping}
- [] map les methodes : HashMap {url: mapping}
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
[ ] Creation classe ModelView:
    - String url
    - HashMap<> data;
[ ] Redirection de la page vers l'url du model and view et affichage des data