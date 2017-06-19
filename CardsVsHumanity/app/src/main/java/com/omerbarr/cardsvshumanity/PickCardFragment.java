package com.omerbarr.cardsvshumanity;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.omerbarr.cardsvshumanity.BusinessLogic.Cards;
import com.omerbarr.cardsvshumanity.Utils.CardAdapter;
import com.omerbarr.cardsvshumanity.Utils.GridSpacingItemDecoration;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PickCardFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PickCardFragment#newInstance} factory method to
 * create an instance of this fragment.`sys
 */
public class PickCardFragment extends Fragment {

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CARDS = "cards";
    private static final String ARG_NUM_OF_ANSWERS = "num_of_answers";

    private View view;

    // views from activity parent
    private Button mButtonReset;
    private Button mButtonOk;
    private TextView mTextCounter;
    private TextView mTextGuidance;

    // view and adapter
    private RecyclerView mCardRecyclerView;
    private ArrayList<String> mCardArrayList;
    private CardAdapter mCardListAdapter;

    private int mAnswerCounter;
    private int[] mPickedanswers;

    // received parameters
    private int[] mReceivedCards;
    private int mNumOfAnswers;

    private OnFragmentInteractionListener mListener;

    public PickCardFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PickCardFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PickCardFragment newInstance(int[] receivedCards, int numOfAnswers) {
        PickCardFragment fragment = new PickCardFragment();
        Bundle args = new Bundle();
        args.putIntArray(ARG_CARDS, receivedCards);
        args.putInt(ARG_NUM_OF_ANSWERS, numOfAnswers);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mReceivedCards = getArguments().getIntArray(ARG_CARDS);
//            mNumOfAnswers = getArguments().getInt(ARG_NUM_OF_ANSWERS);
//        }

        mReceivedCards =  new int[10];
        mReceivedCards[0] = 14;
        mReceivedCards[1] = 8;
        mReceivedCards[2] = 20;
        mReceivedCards[3] = 11;
        mReceivedCards[4] = 2;
        mReceivedCards[5] = 6;
        mReceivedCards[6] = 9;
        mReceivedCards[7] = 17;
        mReceivedCards[8] = 5;
        mReceivedCards[9] = 21;

        mNumOfAnswers = 1;


        // initialize num of picked answers
        mAnswerCounter = 0;
        mPickedanswers = new int[mNumOfAnswers];

        mButtonReset = (Button) getActivity().findViewById(R.id.button_reset);
        mButtonOk  = (Button) getActivity().findViewById(R.id.button_ok);
        mTextCounter = (TextView) getActivity().findViewById(R.id.text_cards_picked);
        mTextCounter.setText(mAnswerCounter+"/"+mNumOfAnswers);
        mTextGuidance = (TextView) getActivity().findViewById(R.id.text_guidance);
        mTextGuidance.setText("Pick up "+mNumOfAnswers+" cards  related to the black card above");

        mButtonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAnswerCounter = 0;
                mTextCounter.setText("0/"+mNumOfAnswers);
                //reset cards and adapter
                mCardArrayList = new ArrayList<>();
                for(int i = 0; i < mReceivedCards.length; i++  ){
                    mCardArrayList.add(Cards.WHITE_CARDS[mReceivedCards[i]]);
                }
                mCardListAdapter = new CardListAdapter(mCardArrayList);
                mCardRecyclerView.setAdapter(mCardListAdapter);
                initSwipe();
            }
        });

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnswerCounter == mNumOfAnswers){
                    // todo send result to activity
                }
                else{
                    Toast.makeText(getContext(),"You need to pick answers in order to send cards",Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_pick_card, container, false);

        // init search contacts view
        mCardRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_card);
        mCardRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL, false));
        mCardRecyclerView.addItemDecoration(new GridSpacingItemDecoration(10, dpToPx(15), true));
        mCardRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mCardRecyclerView.setItemViewCacheSize(20);


        mCardArrayList = new ArrayList<>();
        for(int i = 0; i < mReceivedCards.length; i++  ){
            mCardArrayList.add(Cards.WHITE_CARDS[mReceivedCards[i]]);
        }
        mCardListAdapter = new CardListAdapter(mCardArrayList);
        mCardRecyclerView.setAdapter(mCardListAdapter);
        initSwipe();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }

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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
    /**
     * Converting dp to pixel
     */
    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public class CardListAdapter extends CardAdapter {

        public CardListAdapter(ArrayList<String> arrayList) {
            super(arrayList);
        }

        @Override
        public CardAdapter.CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.white_card_item, viewGroup, false);
            return new CardViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CardAdapter.CardViewHolder viewHolder,final int i) {

            viewHolder.cardContent.setText(mArrayList.get(i));

            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getContext(),"Swipe up to choose",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void initSwipe(){

        final Paint p = new Paint();

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.UP) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.UP){
                    // if the player picked all the cards don't raise the counter
                    if (mAnswerCounter < mNumOfAnswers){
                        mPickedanswers[mAnswerCounter] = mReceivedCards[viewHolder.getAdapterPosition()];
                        mAnswerCounter++;
                        mTextCounter.setText(mAnswerCounter+"/"+mNumOfAnswers);
                        Toast.makeText(getContext(), "Card was picked on index "+viewHolder.getAdapterPosition() , Toast.LENGTH_SHORT).show();
                        viewHolder.itemView.setVisibility(View.INVISIBLE);
                    }
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if (mAnswerCounter < mNumOfAnswers) {
                    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                        View itemView = viewHolder.itemView;
                        if (dY > 0) {
                            itemView.setAlpha((float) (0.9 - dY / 5000));
                        } else {
                            itemView.setAlpha((float) (0.9 + dY / 5000));
                        }
                    }
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
                else
                    // don't move card
                    super.onChildDraw(c, recyclerView, viewHolder, 0, 0, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mCardRecyclerView);

    }
}
