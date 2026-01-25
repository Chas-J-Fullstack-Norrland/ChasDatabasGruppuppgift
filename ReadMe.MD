# Ett gruppprojekt för en EHandels platform.

Meningen med uppgiften är att visa allt som lärts under kursen.
Och visa att man kan hantera större datamängder.

Bevis på fungerande backend finns i test som körs via tester emot lokal databas.

## Instructioner
Klona repot, 
Skapa en postgress databas och fyll i informationen i application.yaml. (tänk på att sätta validate till create första körningen),
När programmet sedan körs så skriv "System help" för listan av kommandon


## Relationer
vi oss av uni och bi-direktionella relationer som hanteras utav JPA.
Uni direktionella används specifikt där vi endast använder relationen i en riktning och kan därför
ignorera hantering, hämtning och sparande av dessa.  Oftast igenom 1:M och 1:1 relationer.

1:1 relationer med mandatory använder vi även nyckelorder Cascade för att låta JPA överlåta hanteringen av dessa genom den entiteten som blir ’förälder’

vid 1:N och N:M så kan vi även hämnta entiteter som har en sak gemensamt utan att hämta den gemensamma entiteten.  Detta används till exempel mellan Produkt och Order_Item.  En användare ska till exempel ej kunna see hur orderID som inkluderar köpt produkt och även inte antal.
## Implementationer
### Lager reservering:
En diskussion som gruppen hade var just “När reserverar vi produkter på lager?” Vissa webshopper som amazon verkar lägga produkter under resrevation när kunden lägger den I kundvagnen,  Medans andra riktlinjer säger att man vill hålla reservationstiden så kort som möjligt och då endast mellan att kunder trycker “KÖP” och att betalningen går igenom. Detta leder även till persistans mellan omstarter då en kund enkelt kan gå tillbaka till en kundvagn då dessa reservationer är kundvagnen.

Vi valde att reservera och minska lagret när kunden lägger produkten I kundvagnen,  detta lagrar vi som en extra entitet som fungerar som en N:M mellan lager-entitet och kund.    Denna innehåller då ProduktID(då Lager är Idmappat efter produkt) och kundID som Primary Key.  Reservationsentiteten innehåller även antalet av den reserverade produkten och när reservationen skapades samt går ut.

I praktik så skulle reservationer släppas utav en schemalagd databas task som raderar reservationer som gått ut.
## Paginering och rapporter
Vi implementerade paginering endast till rapporter.   Detta gör vi genom ett record object som vi mappar värden ifrån en Query,  Specifikt då ifrån OrderItemRepository då denna är perfekt för att hitta vilken produkt som det säljs mest utav. Vi ser även här att vi vill använda relationen för att lättare associera mer en ett ID till denna relation för att då hämnta ut information av produkten och hur många gånger den sålts.
Resterande rapporter hittas I RapportService och kallar på relevant repository och metod för att samla informationen.

## CLI
Enkel tokenizering utav inputstream för att få Domain,Actions,Args,Flags i ett input record
Args = Indexed argumnets
Flags = named variables
Domain = Type of handler we are calling.
Action = Method for above handler, passing input record to it.

## CSV import
Liknande CLI så är CSV indexerade fält av data,  Vi namnger dessa med en header och skickar dem vidare till en klass som implementerar CSVMapperInterface<T>
Dessa klasser lagras i en Hashmap där typen de hanterar är nyckeln och i CSV så har vi kolumn vid namn type. Så att varje rad anropar korrekt mapper.
Att varje rad bara är en entitet är även ett val här, fält som delar samma namn mellan entititeter behöver inte samma type eller format. Samt så 
blir de insatta i ordning som förhindrar KeyConstraintExpections i databasen.

Vi batchar dessa rader i grupper på 1000 då Hibernate är väldigt långsam på att sätta in entititer en för en.
