# Dialog creator 

Parsing text file to dialogs file

## How to build 
```bash 
./gradlew jar 

# builded in build/libs/
```

## Parametets

* `-i` = input Folder with text Files
* `-og` = output graphs  Folder  
* `-orf` = output Routers setting File 
* `-op` = output PhraseText Folder
* `-dc` = default Class of phrases


If you want to create map dialog for all phrases (world router): 
* `--create-world-router` - create map-router for all phrases
* `-rs` = start Point of the map
* `-rn`  = maps dialog Id 

## World Dialog 

World router connected many dialogs with each other. it could be also helpful 
by nested dialogs (when 1 dialog has as `DialoItem` other `Dialog` ) 

## Syntaxis 

```
//comment
$ [dialog settings]  

// ----------- start phrase ----------- 

// if phrase class not set, used default class
---- phrase.id [class.of.phrase]

@ 1 text of phrase
@ 2 text of phrase
@ ..
@ n text of phrase

> answer test (next.phrase.id.1 #phrase_type)
// if phrase type not set, used SIMPLE type
> answer test (next.phrase.id.2)  

// ----------- end phrase ----------- 
```

## Example of creator running

``` bash
java -jar ./dialog-creator-*.jar \'
        -i "./scriptsFolter" \
        -orf "./ouput/routers/routers.json" \
        -og "./output/graphs" \
        -op "output/phrases" \
        --create-world-router \
        -rn "world.router" \
        -rs "world"
```

## Example of text file 

```text
$id=start.dialog
$isResetToStart=false
$startPointId=phrase.start

---- phrase.start [some.example.package.RandomGamePhrase]
//one of these phrases randomly chosen and print in console 
@ random phrase 1
@ random phrase 2


> answer 1   (dialog.1)
> answer 2   (dialog.2)

//start of the next phrase  
---- phrase.1 

@ text of dialog 1

> enter to next dialog (dialog.2 #enter )
> exit (exit #exit) 


---- phrase.2 

@ text of dialog 2


> go to phrase 1 (phrase.1)
//exit from dialog
> exit (exit #exit) 

```
 
 




