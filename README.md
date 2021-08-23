# Monster Maze
This project was created over the course of just over a week.
It mirrors and adds to Mineplex's Monster Maze minigame.

## How to use it
Create a simple maze using snow blocks as maze blocks and
redstone blocks as spawners. Edit the start location and
size of the objects in code. Create a cage and an indicator
(indicator spawns above safe pads). Edit location in code.
Spawn platform size is hard coded.

Edit spawn location in code (Where players spawn when they
join the server, not when the game starts)

Create three signs. On line two, write "classic mode",
"start game", and "join game" (case insensitive)

Put buttons beneath these signs, they are now functional.

I'll publish an example map once I can make sure no personal
data is in it.

## The Good
The code runs for sure. It's a very close recreation of the
original game, it runs well, and does its job. Some of the
code is even rather clean, considering the constraint. The
map isn't hardcoded, it's parsed.

## The bad
A few issues here and there. Hardcoded values for player spawn,
maze start, maze size, etc.

I wanted to add player semi-visibility, but retain the player's
ability to see their own hand. I didn't finish this.

## The ugly
When I got to the powerups, I totally cheaped out on the code.
I was just frantically copy and pasting and I never went back
to implement it properly. I wanted to make it object oriented
but after I had finished everything I didn't feel like going back.

That's probably the worst part of the code.