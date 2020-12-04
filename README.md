# TabZAI

player for the Tablut Challenge 2020 - Fundamentals of Artificial Intelligence - Unibo

## Installation on Ubuntu/Debian 

From console, run these commands to install JDK 8 and ANT:

```
sudo apt update
sudo apt install openjdk-8-jdk -y
sudo apt install ant -y
```

Now, clone the project repository:

```
git clone https://github.com/Zumo09/TablutChallenge2020.git
```

## How to run

To run the player use these command line in the folder ./Tablut/

```
ant *player* -DipAdd *ipAddress* -Dtime *timeout*
```
  
where **player** could be black or white, whereas **ip address** is the server ip address and **timeout** is the timeout for the move selection (seconds)

ex:

```
ant white -DipAdd localhost -Dtime 60
```

further usage instruction in the file rdm.md
