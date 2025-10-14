🎾 Wigell Padel Service

Java Spring Boot Microservice

------------------------------
🏓 Overview

Wigell Padel Service är en mikrotjänst för att hantera padelbanor, spelare och bokningar, framtagen för att integreras med Wigell Gateway.
Tjänsten gör det möjligt för administratörer att hantera padelbanor, prissättning och spelare — och för användare att boka, se och avboka sina matcher.
Applikationen stöder även valutakonvertering (SEK → EURO) via en extern API-tjänst.

------------------------------
🧩 Related Projects

[Wigell Gateway](https://github.com/a-westerberg/WigellGateway) – Central API-gateway och huvudingång till alla Wigell-mikrotjänster.

Wigell MySQL Service – Databascontainer som delar nätverk med mikrotjänsterna.

------------------------------
🚀 Tech Stack

Language: Java 21

Build Tool:	Apache Maven

Framework:	Spring Boot 3.5.6

Libraries:	Spring Web, Spring Data JPA

Database:	MySQL 8.4 (Docker container)

Security:	Spring Security (basic auth)

Logging:	Logback / SLF4J

External API:	API Plugin Currency (SEK → EURO)

Testing:	Spring Boot Test, H2 (test-DB), Mockito

------------------------------
🏁 Getting Started

✅ Prerequisites

Java 21

Maven

Docker (för MySQL och containerisering av tjänsten)

------------------------------
🔌 Ports

Wigell Padel Service:	7575

MySQL:	3306 (internt) / 3307 (externt)

Wigell Gateway:	4545

------------------------------
🔒 Authentication & Roles

Denna mikrotjänst använder Spring Security med basic auth.
Rollerna hanteras gemensamt med övriga Wigell-tjänster.

Roll -	Användarnamn	- Lösenord

ADMIN	- simon -	simon

USER	- alex -	alex

USER -	sara	- sara

USER	- amanda	- amanda

------------------------------
📚 API Endpoints

👑 Admin Endpoints

Metod	 - Endpoint -	Beskrivning

GET	/api/wigellpadel/listcourts	Lista alla banor

POST	/api/wigellpadel/addcourt	Skapa ny padelbana

PUT	/api/wigellpadel/updatecourt/{id}	Uppdatera befintlig bana

DELETE	/api/wigellpadel/removecourt/{id}	Ta bort eller inaktivera bana

GET	/api/wigellpadel/bookings	Lista alla bokningar

PUT	/api/wigellpadel/cancelbooking/{id}	Avboka en användares bokning

🙋 User Endpoints

Metod	- Endpoint -	Beskrivning

GET	/api/wigellpadel/courts	Hämta alla aktiva banor

POST	/api/wigellpadel/bookcourt/{courtId}	Boka bana

GET	/api/wigellpadel/mybookings	Visa användarens bokningar

PUT	/api/wigellpadel/cancel/{bookingId}	Avboka egen bana

------------------------------
🐳 Docker

Tjänsten är containeriserad med timezone Europe/Stockholm och kopplas till samma nätverk som övriga Wigell-tjänster.

Använd script.bat för att bygga och starta containern enkelt.

Docker Network: wigell-network

------------------------------
🛢️ MySQL Database

Parameter -	Värde

Database Name:	wigelldb

Username:	wigelldbassa

Password:	assa

Version:	8.4 (Docker container)

Standardport:

Internt: 3306

Externt: 3307
