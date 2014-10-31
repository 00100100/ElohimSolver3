ElohimSolver3
=============

Solves Sigils of Elohim using backtracking.

This program will take a pool of pieces and jam them in every which way possible into the board.
Unfortunately, this is very, very slow. Some partial solutions are bad and shouldn't be investigated 
further. This program will "prune" bad partial solutions and backtrack.

The pruning methods are:
1) In a sorted list of pieces, if the current piece to place down is the same as the previous piece (was calculated), skip to
the next piece to avoid recalculation.

2) If the current piece does not fit even with all its rotations, backtrack.

3) After placing a piece, check around the immediate area around the piece to see if there are any "bubbles" of spaces
which are of a size that are too small (a bubble of two blocks cannot fit any of the 4-block tetris pieces). If there is a
"bubble" that's smaller than a certain size, backtrack. For one space, the bubbleCheck() will only look only as far as a
certain number so it does look through the whole board. Also there is a 2d grid to store what has been bubbledChecked and the
size it encountered. This is checked before looking around the board for the bubble size.

(I'm thinking of adjusting this some more...)

Feel free to improve this work.

LK00100100@gmail.com
