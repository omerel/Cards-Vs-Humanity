package com.omerbarr.cardsvshumanity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.omerbarr.cardsvshumanity.BusinessLogic.DataTransferred;
import com.omerbarr.cardsvshumanity.Utils.GifImageView;
import com.omerbarr.cardsvshumanity.Utils.JsonConvertor;

import static com.omerbarr.cardsvshumanity.BusinessLogic.GameManager.FINISH_ROUND;


public class RoundWinnerFragment extends Fragment {

    private static final String ARG_DATA = "data";
    private static final String ARG_ID = "data_id";

    private View view;

    // views from activity parent
    private Button mButtonReset;
    private Button mButtonOk;
    private TextView mTextBlackCard;
    private TextView mTextCounter;
    private TextView mTextGuidance;


    private int myId;

    // waiting gif
    private GifImageView mGifImageView;

    private OnFragmentInteractionListener mListener;

    private DataTransferred.RoundData roundData;

    public RoundWinnerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PickWhiteCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RoundWinnerFragment newRoundWinnerFragment(String jsonRoundData,int id) {
        RoundWinnerFragment fragment = new RoundWinnerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, jsonRoundData);
        args.putInt(ARG_ID, id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jsonRoundData = getArguments().getString(ARG_DATA);
            roundData = JsonConvertor.JsonToRoundData(jsonRoundData);
            myId = getArguments().getInt(ARG_ID);
      }

        mButtonReset = (Button) getActivity().findViewById(R.id.button_reset);
        mButtonReset.setEnabled(false);
        mButtonReset.setAlpha((float) 0.15);
        mButtonOk  = (Button) getActivity().findViewById(R.id.button_ok);
        mButtonOk.setBackground(getResources().getDrawable(R.drawable.game_button_style));
        mButtonOk.setText("next round");
        mButtonOk.setEnabled(true);
        mButtonOk.setAlpha((float) 1.0);
        mTextBlackCard = (TextView)getActivity().findViewById(R.id.czar_card);
        mTextBlackCard.setTextSize(32f);
        String czarCard = "Round's winner:\n\n"+"<b>"+roundData.mPlayersNameArrayList.get(roundData.mLastRoundWinner)+"</b>";
        mTextBlackCard.setText( Html.fromHtml(czarCard));
        mTextBlackCard.setGravity(Gravity.CENTER);
        mTextCounter = (TextView) getActivity().findViewById(R.id.text_cards_picked);
        mTextCounter.setText("");
        mTextGuidance = (TextView) getActivity().findViewById(R.id.text_guidance);
        mTextGuidance.setText("Click on next round button when ready");

        if (myId != roundData.mCurrentCzar){
            mButtonOk.setEnabled(false);
            mTextGuidance.setText("Wait for the current Czar proceed next round");
            mButtonOk.setAlpha((float) 0.15);
        }


        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FINISH_ROUND);
                getActivity().sendBroadcast(intent);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_round_winner, container, false);
        mGifImageView = (GifImageView) view.findViewById(R.id.gif);
        //mGifImageView.setGifImageResource(R.drawable.gif_waiting);
        // choose gif
        if (roundData.mLastRoundWinner == myId)
            mGifImageView.setGifImageResource(R.drawable.gif_win);
        else {
            if (roundData.mCurrentCzar == myId)
                mGifImageView.setGifImageResource(R.drawable.gif_waiting);
            else
                mGifImageView.setGifImageResource(R.drawable.gif_loose);
        }
        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int cmd);
    }
}
