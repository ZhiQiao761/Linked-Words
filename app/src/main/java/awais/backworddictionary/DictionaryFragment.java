package awais.backworddictionary;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;

import awais.backworddictionary.asyncs.WordsAsync;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.helpers.WordItemHolder;
import awais.backworddictionary.interfaces.FilterCheck;
import awais.backworddictionary.interfaces.FragmentCallback;

public class DictionaryFragment extends Fragment implements FragmentCallback, FilterCheck {
    private List<WordItem> wordList;
    private RecyclerView recyclerView;
    private Activity activity;
    private InputMethodManager imm;

    private EditText filterSearchEditor;
    private ImageView filterSearchButton;
    private FrameLayout filterView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private final boolean[] filterCheck = {true, true, true};
    public String title;
    DictionaryWordsAdapter wordsAdapter;

    private static int startOffset = -120, endOffset, expandedEndOffset, topPadding, topMargin;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        this.activity = getActivity() != null ? getActivity() : (Activity) context;

        if (Main.tts == null) Main.tts = new TextToSpeech(activity, initStatus -> {
            if (initStatus == TextToSpeech.SUCCESS) {
                if (Main.tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.US);
                else if (Main.tts.isLanguageAvailable(Locale.CANADA) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.CANADA);
                else if (Main.tts.isLanguageAvailable(Locale.UK) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.UK);
                else if (Main.tts.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE)
                    Main.tts.setLanguage(Locale.ENGLISH);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try { Main.tts.stop(); } catch (Exception ignored) {}
        try { Main.tts.shutdown(); } catch (Exception ignored) {}
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,@Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dictionary_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View magicRootView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(magicRootView, savedInstanceState);

        activity = getActivity() == null ? (Activity) getContext() : getActivity();

        if (activity != null)
            imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        wordList = new ArrayList<>();
        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);
        wordsAdapter.setHasStableIds(true);

        swipeRefreshLayout = magicRootView.findViewById(R.id.swipe_container);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeResources(R.color.progress1, R.color.progress2,
                R.color.progress3, R.color.progress4);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);

            if (activity instanceof Main) {
                final Main mainActivity = (Main) activity;
                if (mainActivity.mSearchView != null && mainActivity.mSearchView.isSearchOpen())
                    mainActivity.mSearchView.close(true);

                final String title = (String) activity.getTitle();
                if (!title.equals(getResources().getString(R.string.app_name))) mainActivity.onSearch(title);
                else swipeRefreshLayout.setRefreshing(false);
            }
        });

        startOffset = swipeRefreshLayout.getProgressViewStartOffset();
        expandedEndOffset = (int) (getExpandedOffset() * 1.17f);
        endOffset = (int) (expandedEndOffset - expandedEndOffset * 0.717f);

        recyclerView = magicRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(wordsAdapter);

        topPadding = recyclerView.getPaddingTop();
        topMargin = Math.round(getResources().getDimension(R.dimen.filter_top_margin));

        filterView = magicRootView.findViewById(R.id.filterView);

        final ImageView filterBackButton = magicRootView.findViewById(R.id.filterBack);
        filterSearchButton = magicRootView.findViewById(R.id.filterSettings);
        filterSearchButton.setTag("filter");

        filterSearchEditor = magicRootView.findViewById(R.id.swipeSearch);
        filterSearchEditor.setOnFocusChangeListener((view, b) -> toggleKeyboard(b));
        filterSearchEditor.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence cs, int i, int i1, int i2) {}
            @Override public void onTextChanged(CharSequence cs, int i, int i1, int i2) {
                if (wordList.size() > 2) wordsAdapter.getFilter().filter(cs);
            }
            @Override public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    filterSearchButton.setImageResource(R.drawable.ic_clear);
                    filterSearchButton.setTag("clear");
                } else {
                    filterSearchButton.setImageResource(R.drawable.ic_settings);
                    filterSearchButton.setTag("filter");
                }
            }
        });

        View.OnClickListener onClickListener = view -> {
            if (view == filterBackButton) hideFilter();

            else if (view == filterSearchEditor) toggleKeyboard(true);

            else if (view == filterSearchButton) {
                final Object tag = filterSearchButton.getTag();

                if (!Utils.isEmpty((CharSequence) tag) && tag.equals("filter")) {
                    filterCheck[0] = Main.sharedPreferences.getBoolean("filterWord", true);
                    filterCheck[1] = Main.sharedPreferences.getBoolean("filterDefinition", false);
                    filterCheck[2] = Main.sharedPreferences.getBoolean("filterContain", false);

                    new AlertDialog.Builder(activity).setTitle(getString(R.string.select_filters))
                            .setMultiChoiceItems(new String[] {getString(R.string.words), getString(R.string.defs), getString(R.string.contains)}, filterCheck,
                                    (dialogInterface, i, b) -> {
                                        filterCheck[i] = b;
                                        final String item;
                                        switch (i) {
                                            case 0: item = "filterWord"; break;
                                            case 1: item = "filterDefinition"; break;
                                            case 2: item = "filterContain"; break;
                                            default: item = null; break;
                                        }
                                        if (!Utils.isEmpty(item))
                                            Main.sharedPreferences.edit().putBoolean(item, b).apply();
                                    })
                            .setNeutralButton(R.string.ok, (dialogInterface, i) -> {
                                if (wordList.size() > 2)
                                    wordsAdapter.getFilter().filter(filterSearchEditor.getText());
                                dialogInterface.dismiss();
                            }).show();
                } else {
                    filterSearchEditor.setText("");
                    filterSearchButton.setTag("filter");
                }
            }
        };

        filterBackButton.setOnClickListener(onClickListener);
        filterSearchEditor.setOnClickListener(onClickListener);
        filterSearchButton.setOnClickListener(onClickListener);
    }

    void startWords(String method, String word) {
        if (filterView != null && filterSearchEditor != null) filterSearchEditor.setText("");
        if (Utils.isEmpty(word)) return;
        new WordsAsync(this, word, method, this.activity).execute();
    }

    @Override
    public void done(ArrayList<WordItem> items, final String word) {
        wordList = items == null ? new ArrayList<>() : items;

        swipeRefreshLayout.post(() -> swipeRefreshLayout.setRefreshing(false));

        wordsAdapter = new DictionaryWordsAdapter(activity, wordList);
        wordsAdapter.notifyDataSetChanged();
        recyclerView.setAdapter(wordsAdapter);

        title = word;
        activity.setTitle(title);
    }

    @Override
    public void wordStarted() {
        if (swipeRefreshLayout == null) return;
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setEnabled(true);
            swipeRefreshLayout.setRefreshing(true);
        });
    }

    /**
     * DO NOT CHANGE ANYTHING HERE, I STILL DON'T HAVE
     * ANY IDEA WHAT IS METHOD ABOUT, PLEASE FORGIVE ME.
     * I ASK FOR FORGIVENESS GOD. I SWEAR.
     * 21st June 2019, i think i got a little clue now.
     * @param showFilter either to show or hide filter
     * @param method     no idea what this is. i really forgot.
     */
    @Override
    public void showFilter(boolean showFilter, int method) {
        if (filterView == null) return;

        if (showFilter) {
            if (method == 0 || method == 2) {
                filterView.setVisibility(View.VISIBLE);
                if (method == 0 && filterSearchEditor != null) filterSearchEditor.requestFocus();
            } else if (method == 30)
                filterView.setVisibility(View.GONE);

            if (recyclerView != null) {
                if (method == 0)
                    recyclerView.setPadding(0, topMargin, 0, recyclerView.getPaddingBottom());
                if (wordsAdapter != null && wordsAdapter.getItemCount() > 5) {
                    LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (manager != null && manager.findFirstVisibleItemPosition() <= 3)
                        recyclerView.smoothScrollToPosition(0);
                }

                if (method == 1) return;
                final boolean isRefrehing = swipeRefreshLayout.isRefreshing();
                swipeRefreshLayout.setProgressViewOffset(false, startOffset, expandedEndOffset);
                if (isRefrehing) swipeRefreshLayout.setRefreshing(true);
            }
        } else {
            if (method == 0 || method == 2) filterView.setVisibility(View.GONE);
            else if (method == 30) filterView.setVisibility(View.VISIBLE);

            if (recyclerView != null) recyclerView.setPadding(0, topPadding, 0, recyclerView.getPaddingBottom());

            if (method == 1) return;
            final boolean isRefrehing = swipeRefreshLayout.isRefreshing();
            swipeRefreshLayout.setProgressViewOffset(false, startOffset, endOffset);
            if (isRefrehing) swipeRefreshLayout.setRefreshing(true);
        }
    }

    void hideFilter() {
        showFilter(false, 0);
    }

    boolean isFilterOpen() {
        return filterView != null && filterView.getVisibility() == View.VISIBLE;
    }

    private void toggleKeyboard(boolean show) {
        if (imm == null) imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm == null) return;
        if (show) imm.showSoftInput(filterSearchEditor, 1);
        else imm.hideSoftInputFromWindow(filterSearchEditor.getWindowToken(), 1);
    }

    void scrollRecyclerView(boolean directionUp) {
        if (recyclerView == null) return;
        recyclerView.smoothScrollToPosition(directionUp ? 0 : 5000);
    }

    private float getExpandedOffset() {
        TypedValue tv = new TypedValue();
        if (activity == null) activity = getActivity();
        if (activity != null && activity.getTheme().resolveAttribute(R.attr.actionBarSize, tv, true))
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        return swipeRefreshLayout == null ? 250.0f : swipeRefreshLayout.getProgressViewEndOffset() * 2.0f;
    }

    @SuppressWarnings( "unchecked" )
    void closeExpanded() {
        if (wordsAdapter == null) return;
        wordsAdapter.refreshShowDialogEnabled();

        final LinkedHashSet[] hashSets = wordsAdapter.getHashSets();

        final LinkedHashSet<WordItemHolder> wordHoldersSet = hashSets[1];
        for (WordItemHolder holder : wordHoldersSet) holder.cardView.setCardBackgroundColor(-1);

        final LinkedHashSet<WordItem> expandedWordsSet = hashSets[0];
        for (WordItem wordItem : expandedWordsSet) wordItem.setExpanded(false);

        wordsAdapter.notifyDataSetChanged();
    }
}