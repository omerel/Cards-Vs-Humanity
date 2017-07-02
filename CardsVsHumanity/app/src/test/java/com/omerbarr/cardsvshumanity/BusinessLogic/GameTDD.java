package com.omerbarr.cardsvshumanity.BusinessLogic;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by barrinbar on 02/07/2017.
 */
public class GameTDD {
    private ArrayList<String> playersList = new ArrayList<String>(Arrays.asList("Omer", "Barr", "Rachael", "Adi"));
    private GameData gd;
    private DataTransferred.RoundData rd;
    private int oldCzar;
    private int oldRound;

    @Before
    public void setUp() throws Exception {
        gd = new GameData(playersList);
        rd = gd.getRoundData();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void initRound() throws Exception {
        Assert.assertEquals("failure - round not initialized", 0, gd.getRound());
    }

    @Test
    public void initCzar() throws Exception {
        Assert.assertEquals("failure - czar not initialized", -1, gd.getCurrentCzar());
    }

    @Test
    public void spreadWhiteCardsToAllPlayers() throws Exception {
        rd = gd.getRoundData();

        // How to validate spreadWhiteCardsToAllPlayers??;
        for (int i = 0; i < playersList.size(); i++) {
            Assert.assertEquals("failure - cards not dealt", 10, rd.mPlayersCardsPull[i].size());
        }
        Assert.assertNotSame("failure - duplicate cards dealt", rd.mPlayersCardsPull[0], rd.mPlayersCardsPull[1]);
        Assert.assertNotSame("failure - duplicate cards dealt", rd.mPlayersCardsPull[0], rd.mPlayersCardsPull[2]);
        Assert.assertNotSame("failure - duplicate cards dealt", rd.mPlayersCardsPull[0], rd.mPlayersCardsPull[3]);
        Assert.assertNotSame("failure - duplicate cards dealt", rd.mPlayersCardsPull[1], rd.mPlayersCardsPull[2]);
        Assert.assertNotSame("failure - duplicate cards dealt", rd.mPlayersCardsPull[1], rd.mPlayersCardsPull[3]);
        Assert.assertNotSame("failure - duplicate cards dealt", rd.mPlayersCardsPull[2], rd.mPlayersCardsPull[3]);
    }

    @Test
    public void removeCardFromPlayer() throws Exception {
        rd = gd.getRoundData();

        // Before remove
        ArrayList<Integer> currCards = rd.mPlayersCardsPull[0];
        int[] arrRem = {currCards.get(0), currCards.get(1)};
        gd.removeCardFromPlayer(0, arrRem);

        // After remove
        Assert.assertEquals("failure - cards not removed", currCards.size()-2, rd.mPlayersCardsPull[0].size());
    }

    @Test
    public void startRound() throws Exception {

        // Remove cards to validate dealing new cards
        //this.removeCardFromPlayer();

        oldCzar = rd.mCurrentCzar;
        oldRound = gd.getRound();

        gd.startRound();
    }

    @Test
    public void checkRoundAdvanced() throws Exception {

        Assert.assertNotSame("failure - round not advanced", oldRound, gd.getRound());
    }

    @Test
    public void checkDealingCards() throws Exception {
        rd = gd.getRoundData();
        Assert.assertEquals("failure - cards not dealt", 10, rd.mPlayersCardsPull[0].size());
    }


    @Test
    public void checkCzar() throws Exception {

        Assert.assertNotSame("failure - Czar not changed", oldCzar, gd.getCurrentCzar());
    }


    @Test
    public void pickBlackCard() throws Exception {
        gd.pickBlackCard(23);
        rd = gd.getRoundData();
        Assert.assertEquals("failure - black card not removed from deck", -1, rd.mBlackCards.get(23));
    }

    @Test
    public void addScoreToPlayer() throws Exception {
        rd = gd.getRoundData();
        int oldScore = gd.getScoreTable()[0];

        gd.addScoreToPlayer(0);

        Assert.assertEquals("failure - score not increased", oldScore + 1, gd.getScoreTable()[0]);
    }
}