````
public class Board {
    Spot[][] spots;
    public Board() {
        this.startBoard();
    }
}
````

````
// set up the spots with black pieces
spots[7][0] = new Spot(7, 0, new Rook(false));
spots[7][1] = new Spot(7, 1, new Knight(false));
//…
````

````
// set up the spots with no pieces
for (int i = 2; i < 6; i++) {
    for (int j = 0; j < 8; j++) {
        spots[i][j] = new Spot(i, j, NULL);
    }
}
````

````
public abstract class Piece {
    private boolean killed = false;
    private boolean white = false;
    public Piece(boolean white) {
        this.setWhite(white);
    }
    public boolean isWhite() {
       return this.white == true;
    }
    
    public void setWhite(boolean white) {
        this.white = white;
    }
    public boolean isKilled() {
        return this.killed == true;
    }
    public void setKilled(boolean killed) {
        this.killed = killed;
    }
    public abstract boolean moveValid(Board board, Spot start,
                                      Spot end);
}
````

````
public class King extends Piece {
    public King(boolean white) {
        super(white);
    }
    //code to return True if move is Valid, false if move is invalid
    @Override
    public boolean moveValid(Board board, Spot start, Spot end) {
       if(end.getPiece().isWhite() == this.isWhite()) {
           return false;
       }
       int x = Math.abs(start.getX() — end.getX());
       int y = Math.abs(start.getY() — end.getY());
       if(x < 2 && y < 2 && x+y > 0 ) {
           return true;
       }
   }
}
````

````
public class Spot {
    private int x;
    private int y;
    private Piece piece;
    public Spot(int x, int y, Piece piece) {
        this.setPiece(piece);
        this.setX(x);
        this.setY(y);
    }
}
````

````
public class Player {
    private Person person;
    private boolean whiteSide = false;
    public Player(Person person, boolean whiteSide){
        this.person = person;
        this.whiteSide = whiteSide;
    }
    public boolean isWhiteSide() {
        return this.whiteSide == true;
    }
}
````

````
public class Person {
    private String name;
    private String email;
}
````


````
public class Move {
    private Player player;
    private Spot start;
    private Spot end;
    private Piece pieceMoved;
    private Piece pieceKilled;
 
    public Move(Player player, Spot start, Spot end){
        this.player = player;
        this.start = start;
        this.end = end;
        this.pieceMoved = start.getPiece();
    }
}
````

````
public class Game {
    private Player[] players;
    private Board board;
    private Player currentPlayer;
    private GameStatus status;
    private List<Move> movesPlayed;
}
````

````
private void startGame(Player p1, Player p2) {
    players[0] = p1;
    players[1] = p2;
    board.startBoard();
    if(p1.isWhiteSide()) {
        this.currentPlayer = p1;
    } else {
        this.currentPlayer = p2;
    }
}
````

````
public boolean getStatus() {
    return this.status;
}
public void setStatus(GameStatus status) {
    this.status = status;
}
````

````
public boolean playerMove(Player player, int startX, int startY, 
                                         int endX, int endY) {
    Spot start = board[startX][startY];
    Spot end = board[endX][endY];
    Piece sourcePiece = start.getPiece();
    if (!sourcePiece.moveValid(board, start, end))
        return false;
    //
}
````

````
Piece destPiece = end.getPiece();
if (destPiece!=NULL){
    destPiece.setKilled(true);
}
end.setPiece(sourcePiece);
````

````
if (destPiece instanceof King && 
    sourcePiece.isWhite() != destPiece.isWhite()) {
    
    if(player.isWhiteSide()) {
        this.setStatus(GameStatus.WHITEWIN);
    } else {
        this.setStatus(GameStatus.BLACKWIN);
    }
}
````

````
public boolean playerMove(Player player, int startX, int startY, 
                                         int endX, int endY) {
    Spot start = board[startX][startY];
    Spot end = board[endX][endY];
    Piece sourcePiece = start.getPiece();
    if (!sourcePiece.moveValid(board, start, end))
        return false;
    Piece destPiece = end.getPiece();
    if (destPiece!=NULL){
        destPiece.setKilled(true);
    }
    end.setPiece(sourcePiece);
    if ( destPiece instanceof King && 
         sourcePiece.isWhite() != destPiece.isWhite()) {
        if(player.isWhiteSide()) {
            this.setStatus(GameStatus.WHITEWIN);
        } else {
            this.setStatus(GameStatus.BLACKWIN);
        }
    }
    if(this.currentPlayer == p1) {
        this.currentPlayer = p2;
    } else {
        this.currentPlayer = p1;
    }
    return true;
}
````

https://medium.com/double-pointer/system-design-interview-a-two-player-online-chess-game-4a150ad8ea78