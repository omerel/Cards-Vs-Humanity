package com.omerbarr.cardsvshumanity.BusinessLogic;

import java.util.ArrayList;

public class DataTransferred {


    public static class RoundData{
        public int[] mScoreTable;
        public int mRound;
        public  int mCurrentCzar;
        public ArrayList<String> mBlackCards;
        public ArrayList<String> mPlayersNameArrayList;
        public ArrayList<Integer>[] mPlayersCardsPull;

        public RoundData(int[] mScoreTable, int mRound, int mCurrentCzar,ArrayList<String> mBlackCards,
                         ArrayList<String> mPlayersNameArrayList,
                         ArrayList<Integer>[] mPlayersCardsPull) {
            this.mScoreTable = mScoreTable;
            this.mRound = mRound;
            this.mCurrentCzar = mCurrentCzar;
            this.mBlackCards = mBlackCards;
            this.mPlayersNameArrayList = mPlayersNameArrayList;
            this.mPlayersCardsPull = mPlayersCardsPull;
        }
    }

    public static class CzarData{
        public int pickedBlackCard;

        public CzarData(int pickedBlackCard) {
            this.pickedBlackCard = pickedBlackCard;
        }
    }

    public static class PlayerData{
        public int[] pickedAnswers;

        public PlayerData(int[] pickedAnswers) {
            this.pickedAnswers = pickedAnswers;
        }
    }
}
