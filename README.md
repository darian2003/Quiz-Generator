# TEMA 1 POO

## Cerinta 

* Se cere implementarea unui generator simplu de chestionare care presupune crearea unui program in java ce poate primi un set limitat de comenzi si intoarce rezultatul acestora.

## Implementare

### Descrierea claselor

* Tema se concentreaza pe crearea si utilizarea unei baze de date in care vor fi stocate persistent informatii despre utilizatori, intrebari, chestionare si solutii sub forma unor fisiere de tip CSV. 

* Astfel, am folosit clasa FileOp pentru crearea acestor fisiere. Pentru fiecare fisier am creat o functie de citire si de scriere pentru stocarea si preluarea datelor (in formatul preferat de mine si explicat prin comentarii) catre baza de date reprezentata de clasa Database. Am preferat ca modul de scriere in fisier sa fie de tip "suprascriere" si nu "append".

* Clasa Tema1, ce contine doar metoda Main, este ca o linie de comanda care primeste comanda trimisa de utilizator din apeleaza metoda aferenta din clasa Command.

* Clasele User, Question, Quizz si Answer contin atat campuri de variabile si constructori, cat si metode specifice care usureaza implementarea comenzilor.


### Descrierea implementarii comenzilor

* Excluzand "-create-user", fiecare comanda are ca prim pas logarea utilizatorului in sistem folosind parametrii trimisi prin linia de comanda si baza noastra de date. Se vor apela astfel metodele corespunzatoare din clasa User, se vor verifica toate conditiile cerute si se vor intoarce mesajele de eroare aferente sau cel de succes.

* "-create-question" separa argumentele din linia de comanda si creeaza intrebarea cu datele corespunzatoare, realizand si toate verificarile necesare. ID-ul este setat incremental. In urma crearii intreabarii cu succes vom seta fiecarui raspuns ponderea pe care o are, lucru realizat astfel: observam ca fiecare raspuns are aceeasi pondere indiferent de combinatia de raspunsuri bifate. Asadar, calculam prin metoda setPointsPerAnswer punctele specifice fiecarui raspuns in functie de tipul intrebarii (single/multiple) si de  numarul de raspunsuri corecte/gresite, iar ulterior, intrucat raspunsurile sunt independente unul fata de celalalt, la comanda de "-submit-quiz" va trebui doar sa insumam aceste puncte.

* Comenzile "-get-questin-id-by-text" si "-get-all-questions" folosesc ArrayList-ul "questions" din baza de date pentru a prelua si afisa informatiile dorite.

* Comanda "-create-quiz" creeaza un obiect de tip Quizz si adauga in ArrayList-ul "questions" referintele tuturor intrebarilor dorite, trimise sub forma de ID prin linia de comanda.

* Comanda "-submit-quiz" verifica daca utilizatorul logat a mai rezolvat acest quiz, apoi insumeaza punctele aferente tuturor raspunsurilor primite in linia de comanda si afiseaza scorul in formatul dorit. Adaugam apoi quizul la ArrayList-ul de quizuri rezolvate ale userului curent si scriem detaliile in fisierul de solutii.

* Comanda "-delete-quiz" cauta quiz-ul dorit inbaza de date folosind ID-ul primit si il elimina din ArrayList. Ne asiguram intai ca userul care efectueaza aceasta comanda este acelasi cu cel care a creat quiz-ul.

* Comanda "-get-solutions" afiseaza toate elementele din ArrayList-ul de quiz-uri rezolvate ale utilizatorului curent.

* Comanda "-cleanup-all" goleste toate ArrayList-urile din Database si rescrie toate fisierele cu un string gol.

## Bibliografie

* Cursurile 2,3,4 si 5 si Laboratoarele 2,3 si 4 

## Bonus

### Cazuri limita

* Comanda "-delete-quiz" nu specifica daca quizurile sterse trebuie sa dispara doar din baza de date astfel incat sa nu mai poata fi raspunse sau trebuie sterse si din istoricul de quizuri rezolvate ale utilizatorilor. Astfel, comanda "-get-solutions" poate avea premize diferite.

* Adaugarea (in cazul intrebarilor cu raspuns multiplu) posibilitatii de a crea intrebari fara niciun raspuns corect/gresit. Astfel, pentru a primi intregul punctaj va trebui fie sa bifam toate raspunsurile fie niciunul. 

* Adaugarea a diferite nivele de importanta/prioritate a intrebarilor: intrebarile cu prioritate mai mare vor avea o pondere mai mare in cadrul chestioarelor


### Refactorizare

* Adaugarea unei comenzi care primeste ID-ul unui quiz si afiseaza un top al tuturor utilizatorilor care au incarcat raspunsurile

* Adaugarea unei comenzi care afiseaza un intreg chestionar ( fiecare intrebare alaturi de raspunsurile ei) si un simbol precum 'x' in dreptul raspunsurilor bifate. Cred ca ar face aceasta interfata mai interactiva cu utilizatorul.

* Adaugarea unei comenzi care calculeaza o medie/nota sau un nivel de experienta al utilizatorului pe baza tuturor rezultatelor din istoricul sau de chestionare rezolvate

