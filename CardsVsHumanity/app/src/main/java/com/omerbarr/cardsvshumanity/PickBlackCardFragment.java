package com.omerbarr.cardsvshumanity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.omerbarr.cardsvshumanity.BusinessLogic.Cards;
import com.omerbarr.cardsvshumanity.BusinessLogic.DataTransferred;
import com.omerbarr.cardsvshumanity.Utils.GifImageView;
import com.omerbarr.cardsvshumanity.Utils.JsonConvertor;

import java.util.ArrayList;
import java.util.Random;

import static com.omerbarr.cardsvshumanity.BusinessLogic.GameManager.UPDATE_CZAR_DATA;
import static com.omerbarr.cardsvshumanity.GameActivity.SOUND_FLIP;
import static com.omerbarr.cardsvshumanity.GameActivity.UPDATE_MANAGER_WITH_CZAR_DATA;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PickBlackCardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PickBlackCardFragment#newPickBlackCardFragment} factory method to
 * create an instance of this fragment.`sys
 */
public class PickBlackCardFragment extends Fragment {

    private static final String ARG_DATA = "data";

    private View view;

    // views from activity parent
    private Button mButtonReset;
    private Button mButtonOk;
    private TextView mTextBlackCard;
    private TextView mTextCounter;
    private TextView mTextGuidance;
    private TextView mCardContent;


    private DataTransferred.RoundData roundData;
    private int pickedCard;

    // waiting gif
    private GifImageView mGifImageView;

    private String[] mCzarCard;
    private final String SPACE = "_______";

    private OnFragmentInteractionListener mListener;

    public PickBlackCardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PickWhiteCardFragment.
     */
    public static PickBlackCardFragment newPickBlackCardFragment(String jsonRoundData) {
        PickBlackCardFragment fragment = new PickBlackCardFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DATA, jsonRoundData);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String jsonRoundData = getArguments().getString(ARG_DATA);
            roundData = JsonConvertor.JsonToRoundData(jsonRoundData);
      }

        mButtonReset = (Button) getActivity().findViewById(R.id.button_reset);
        mButtonReset.setEnabled(true);
        mButtonReset.setAlpha((float) 1.0);
        mButtonReset.setText("shuffle");
        mButtonOk  = (Button) getActivity().findViewById(R.id.button_ok);
        mButtonOk.setText("pick card");
        mButtonOk.setEnabled(true);
        mButtonOk.setAlpha((float) 1.0);
        mTextBlackCard = (TextView)getActivity().findViewById(R.id.czar_card);
        String czarCard = "";
        mTextBlackCard.setText(czarCard);
        mTextCounter = (TextView) getActivity().findViewById(R.id.text_cards_picked);
        mTextCounter.setText("");
        mTextGuidance = (TextView) getActivity().findViewById(R.id.text_guidance);
        mTextGuidance.setText("Read your chosen card to your friends and press the pick card button");


        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pickedCard =   shuffleBlackCard(roundData.mBlackCards);
                mCardContent = (TextView)view.findViewById(R.id.card_content);
                String card = Cards.BLACK_CARDS[pickedCard];
                mCzarCard = card.split("_");
                mCardContent.setText(getCzarCard());
            }
        });

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mButtonOk.setAlpha((float) 0.15);
                mButtonOk.setEnabled(false);
                mButtonReset.setAlpha((float) 0.15);
                mButtonReset.setEnabled(false);

                mListener.onFragmentInteraction(SOUND_FLIP);

                // broadcast game manger picked card
                Intent intent =  new Intent(UPDATE_CZAR_DATA);
                intent.putExtra("data",pickedCard);
                getActivity().sendBroadcast(intent);

                //if czar is manager broadcast activity picked game
                Intent intent1 =  new Intent(UPDATE_MANAGER_WITH_CZAR_DATA);
                intent1.putExtra("data",JsonConvertor.convertToJson(new DataTransferred.CzarData(pickedCard)));
                getActivity().sendBroadcast(intent1);

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pick_black_card, container, false);

        pickedCard = shuffleBlackCard(roundData.mBlackCards);
        mCardContent = (TextView)view.findViewById(R.id.card_content);
        String card = Cards.BLACK_CARDS[pickedCard];
        mCzarCard = card.split("_");
        mCardContent.setText(getCzarCard());
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
        void onFragmentInteraction(int sound);
    }

    public int shuffleBlackCard(ArrayList arrayList){
        int card = 0;
        if (arrayList.size() > 0) {
            Random random = new Random();
            card = random.nextInt(arrayList.size());

        }
        return card;
    }

    private Spanned getCzarCard(){

        String text = "";
        if (mCzarCard.length == 1)
            text= mCzarCard[0]+": "+SPACE;
        else{
            for(int i =0 ;i<mCzarCard.length; i++){
                text+=mCzarCard[i];
                if (i != mCzarCard.length-1)
                    text+=SPACE;
            }
        }
        return Html.fromHtml(text);
    }
}
