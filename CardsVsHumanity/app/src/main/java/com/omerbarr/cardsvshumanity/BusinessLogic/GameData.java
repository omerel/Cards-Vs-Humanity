package com.omerbarr.cardsvshumanity.BusinessLogic;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by omer on 21/06/2017.
 */

public class GameData implements GameCommandsConstants {

    private int[] mScoreTable;
    private int mRound;
    private  int mCurrentCzar;
    private ArrayList<String> mWhiteCards;
    private ArrayList<String> mBlackCards;
    private ArrayList<String> mPlayersNameArrayList;
    private ArrayList<Integer>[] mPlayersCardsPull;
    private int mLastRoundWinnerId;
    private int mPickedBlackCard;

    public GameData(ArrayList<String> playersNameArrayList) {

        this.mPlayersNameArrayList = playersNameArrayList;

        // initial score table
        mScoreTable = new int[mPlayersNameArrayList.size()];
        for(int i = 0; i < mPlayersNameArrayList.size(); i++)
            mScoreTable[i] = 0;

        // initial game values
        mRound = 0;
        mCurrentCzar = -1;
        mWhiteCards = new ArrayList<>();
        for(int i = 0; i < Cards.WHITE_CARDS.length; i++ )
            mWhiteCards.add(Cards.WHITE_CARDS[i]);
       // Collections.addAll(mWhiteCards, Cards.WHITE_CARDS);
        mBlackCards = new ArrayList<>();
        for(int i = 0; i < Cards.BLACK_CARDS.length; i++ )
            mBlackCards.add(Cards.BLACK_CARDS[i]);
       // Collections.addAll(mBlackCards, Cards.BLACK_CARDS);
        mPlayersCardsPull = new ArrayList[mPlayersNameArrayList.size()];
        for (int i=0; i < mPlayersCardsPull.length; i++)
            mPlayersCardsPull[i] = new ArrayList<>();
        mLastRoundWinnerId = 0;
        spreadWhiteCardsToAllPlayers();
    }

    private int pickWhiteCard(){
        int card = 0;
        if (mWhiteCards.size() > 0) {
            Random random = new Random();
            card = random.nextInt(mWhiteCards.size());
            mWhiteCards.remove(card);
        }
        return card;
    }

    public void removeCardFromPlayer(int player,int[] cards){
        for (int i = 0 ; i < cards.length; i++){
            if (mPlayersCardsPull.length > player)
            {
                int index = mPlayersCardsPull[player].indexOf(cards[i]);
                if (index != -1)
                    mPlayersCardsPull[player].remove(index);
            }
        }

    }

    public int shuffleBlackCard(){
        int card = 0;
        if (mBlackCards.size() > 0) {
            Random random = new Random();
            card = random.nextInt(mBlackCards.size());
        }
        return card;
    }

    public void pickBlackCard(int card){
        mBlackCards.remove(card);
    }

    public void addScoreToPlayer(int playerIndex){
        mScoreTable[playerIndex]++;
        mLastRoundWinnerId = playerIndex;
    }

    public int getLastRoundWinner(){return mLastRoundWinnerId;}

    public int[] getScoreTable(){return mScoreTable;}

    public int getRound(){return mRound;}

    public int getCurrentCzar(){return mCurrentCzar;}

    public void spreadWhiteCardsToAllPlayers(){

        while ( mWhiteCards.size() > 0 ){
            for( int i = 0; i < mPlayersCardsPull.length; i++ ){
                if (mPlayersCardsPull[i].size() < MAX_CARDS)
                    mPlayersCardsPull[i].add(pickWhiteCard());
            }
            // if all players got all cards (the last player is an indicator) break loop
            if ( mPlayersCardsPull[mPlayersCardsPull.length-1].size() == MAX_CARDS)
                break;
        }
    }

    public DataTransferred.RoundData getRoundData(){
        return new DataTransferred.RoundData(mScoreTable,mRound,mCurrentCzar,mBlackCards,
                mPlayersNameArrayList,mPlayersCardsPull,mLastRoundWinnerId);
    }

    // returns the current czar
    public int startRound(){
        spreadWhiteCardsToAllPlayers();
        mRound++;
        if((mCurrentCzar+1) == mPlayersNameArrayList.size())
            mCurrentCzar = -1;
        mCurrentCzar++;
        return  mCurrentCzar;
    }

}
