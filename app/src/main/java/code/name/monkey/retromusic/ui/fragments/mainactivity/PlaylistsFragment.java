package code.name.monkey.retromusic.ui.fragments.mainactivity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.retro.musicplayer.backend.Injection;
import com.retro.musicplayer.backend.loaders.GenreLoader;
import com.retro.musicplayer.backend.model.Genre;
import com.retro.musicplayer.backend.model.Playlist;
import com.retro.musicplayer.backend.model.smartplaylist.HistoryPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.LastAddedPlaylist;
import com.retro.musicplayer.backend.model.smartplaylist.MyTopTracksPlaylist;
import com.retro.musicplayer.backend.mvp.contract.PlaylistContract;
import com.retro.musicplayer.backend.mvp.presenter.PlaylistPresenter;
import com.retro.musicplayer.backend.util.schedulers.BaseSchedulerProvider;
import com.retro.musicplayer.backend.util.schedulers.SchedulerProvider;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import code.name.monkey.retromusic.R;
import code.name.monkey.retromusic.ui.adapter.GenreAdapter;
import code.name.monkey.retromusic.ui.adapter.PlaylistAdapter;
import code.name.monkey.retromusic.ui.fragments.base.AbsLibraryPagerFragment;
import code.name.monkey.retromusic.util.NavigationUtil;
import code.name.monkey.retromusic.util.PreferenceUtil;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by hemanths on 19/08/17.
 */

public class PlaylistsFragment extends AbsLibraryPagerFragment
        implements PlaylistContract.PlaylistView {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.genre_recycler_view)
    RecyclerView genreRecyclerView;
    @BindView(R.id.genre_container)
    ViewGroup mViewGroup;
    private PlaylistPresenter mPlaylistPresenter;
    private PlaylistAdapter mPlaylistAdapter;
    private Unbinder unBinder;
    private CompositeDisposable mDisposable;
    private BaseSchedulerProvider mProvider;

    public static PlaylistsFragment newInstance() {
        Bundle args = new Bundle();
        PlaylistsFragment fragment = new PlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @OnClick({R.id.history, R.id.last_added, R.id.top_tracks})
    void onClicks(View view) {
        Activity activity = getActivity();
        if (activity != null) {
            switch (view.getId()) {
                case R.id.history:
                    NavigationUtil.goToPlaylistNew(activity, new HistoryPlaylist(activity));
                    break;
                case R.id.last_added:
                    NavigationUtil.goToPlaylistNew(activity, new LastAddedPlaylist(activity));
                    break;
                case R.id.top_tracks:
                    NavigationUtil.goToPlaylistNew(activity, new MyTopTracksPlaylist(activity));
                    break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_playlist, container, false);
        unBinder = ButterKnife.bind(this, layout);
        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unBinder.unbind();
        mDisposable.clear();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDisposable = new CompositeDisposable();
        mProvider = new SchedulerProvider();
        mPlaylistPresenter = new PlaylistPresenter(Injection.provideRepository(getContext()), this);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupPlaylists();
        setupGenre();
    }

    private void setupPlaylists() {
        title.setText(R.string.playlists);
        mPlaylistAdapter = new PlaylistAdapter(getLibraryFragment().getMainActivity(), new ArrayList<>(), R.layout.item_list, null);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mPlaylistAdapter);
    }

    private void setupGenre() {
        GenreAdapter genreAdapter = new GenreAdapter(getActivity(), R.layout.item_list);
        genreRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        genreRecyclerView.setItemAnimator(new DefaultItemAnimator());
        genreRecyclerView.setAdapter(genreAdapter);

        mDisposable.add(GenreLoader.getAllGenres(getContext())
                .subscribeOn(mProvider.io())
                .observeOn(mProvider.ui())
                .map(genres -> {
                    ArrayList<Genre> list = new ArrayList<>();
                    for (Genre genre : genres) if (genre.songCount > 0) list.add(genre);
                    return list;
                })
                .subscribe(genres -> {
                    if (genres.size() > 0 && !PreferenceUtil.getInstance(getContext()).isGenreShown()) {
                        mViewGroup.setVisibility(View.VISIBLE);
                        genreAdapter.swapData(genres);
                    }
                }));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPlaylistAdapter.getDataSet().isEmpty())
            mPlaylistPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPlaylistPresenter.unsubscribe();
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        mPlaylistPresenter.loadPlaylists();
    }

    @Override
    public void loading() {

    }

    @Override
    public void showEmptyView() {
        mPlaylistAdapter.swapDataSet(new ArrayList<Playlist>());
    }

    @Override
    public void completed() {

    }

    @Override
    public void showList(ArrayList<Playlist> songs) {
        mPlaylistAdapter.swapDataSet(songs);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.removeItem(R.id.action_shuffle_all);
        menu.removeItem(R.id.action_sort_order);
    }

}
