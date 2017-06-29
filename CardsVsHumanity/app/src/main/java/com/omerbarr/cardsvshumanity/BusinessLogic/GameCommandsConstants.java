package com.omerbarr.cardsvshumanity.BusinessLogic;


public interface GameCommandsConstants {

    String ROUND = "Round: ";
    String SCORE = "Score: ";

    int MAX_TIME_FOR_RESPONED = 5000;
    int MAX_CARDS = 10;

    int CMD_START_GAME = 111;
    int ACK_START_GAME = 112;

    int CMD_START_ROUND = 113;
    int ACK_START_ROUND = 114;

    int CMD_REVEAL_BLACK_CARD = 115;
    int ACK_REVEAL_BLACK_CARD = 116;

    int UPDATE_PLAYER_ANSWER = 117;

    int CMD_SHOW_ROUND_PLAYERS_ANSWERS = 120;

    int CMD_SHOW_ROUND_RESULT = 118;
    int ACK_SHOW_ROUND_RESULT = 119;

    int CMD_FINISH_ROUND = 121;

    int UPDATE_ROUND_WINNER = 122;





}
