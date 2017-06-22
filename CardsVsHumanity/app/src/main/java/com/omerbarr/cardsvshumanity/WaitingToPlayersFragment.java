package com.omerbarr.cardsvshumanity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.omerbarr.cardsvshumanity.Utils.GifImageView;

import static com.omerbarr.cardsvshumanity.BusinessLogic.PlayerManager.BROAD_CAST_ACK_WAITING;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WaitingToPlayersFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WaitingToPlayersFragment#newWaitingToPlayersFragment} factory method to
 * create an instance of this fragment.`sys
 */
public class WaitingToPlayersFragment extends Fragment {

    private View view;

    // views from activity parent
    private Button mButtonReset;
    private Button mButtonOk;
    private TextView mTextBlackCard;
    private TextView mTextCounter;
    private TextView mTextGuidance;

    // waiting gif
    private GifImageView mGifImageView;

    private OnFragmentInteractionListener mListener;

    public WaitingToPlayersFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PickWhiteCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WaitingToPlayersFragment newWaitingToPlayersFragment() {
        WaitingToPlayersFragment fragment = new WaitingToPlayersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
      }
        mButtonReset = (Button) getActivity().findViewById(R.id.button_reset);
        mButtonReset.setEnabled(false);
        mButtonReset.setAlpha((float) 0.15);
        mButtonOk  = (Button) getActivity().findViewById(R.id.button_ok);
        mButtonOk.setEnabled(false);
        mButtonOk.setAlpha((float) 0.15);
        mTextBlackCard = (TextView)getActivity().findViewById(R.id.czar_card);
        String czarCard = "";
        mTextBlackCard.setText(czarCard);
        mTextCounter = (TextView) getActivity().findViewById(R.id.text_cards_picked);
        mTextCounter.setText("");
        mTextGuidance = (TextView) getActivity().findViewById(R.id.text_guidance);
        mTextGuidance.setText("Waiting for players pick their answers");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_wait_to_players, container, false);
        mGifImageView = (GifImageView) view.findViewById(R.id.gif);
        mGifImageView.setGifImageResource(R.drawable.gif_waiting);
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
