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
