package org.zalando.switchman.ui;

import org.zalando.switchman.Injector;
import org.zalando.switchman.ItemId;
import org.zalando.switchman.data.RecommendationDataSource;
import org.zalando.switchman.data.SearchDataSource;

import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class Presenter {
    private RecommendationDataSource recommendationDataSource;

    public RecommendationDataSource getRecommendationDataSource() {
        return recommendationDataSource;
    }

    public void setRecommendationDataSource(RecommendationDataSource recommendationDataSource) {
        this.recommendationDataSource = recommendationDataSource;
    }

    private SearchDataSource searchDataSource;

    public SearchDataSource getSearchDataSource() {
        return searchDataSource;
    }

    public void setSearchDataSource(SearchDataSource searchDataSource) {
        this.searchDataSource = searchDataSource;
    }

    private CompositeSubscription subscription = new CompositeSubscription();

    public CompositeSubscription getSubscription() {
        return subscription;
    }

    public Presenter() {
    }

    void inject() {
        setRecommendationDataSource(Injector.createRecommendationDataSource());
        setSearchDataSource(Injector.createSearchDataSource());
    }

    void search(MainActivity mainActivity) {
        getSubscription().add(getSearchDataSource().search()
                .subscribe(recipes -> mainActivity.displayRecipes(recipes, mainActivity.presenter.getRecommendationStateChecker(), mainActivity.presenter.getRecommendationListener(mainActivity))));
    }

    RecommendationStateChecker getRecommendationStateChecker() {
        return new RecommendationStateChecker(getRecommendationDataSource());
    }

    RecommendationView.RecommendationListener getRecommendationListener(MainActivity mainActivity) {
        return new RecommendationView.RecommendationListener() {

            @Override
            public void recommend(ItemId itemId, RecommendationView.RequestListener requestListener) {
                getSubscription().add(getRecommendationDataSource().addItem(itemId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> mainActivity.onRecommendationRequestSuccess(response, requestListener,
                                        "Due to an error request to start recommending is failed"),
                                throwable -> mainActivity.onRecommendationRequestFail(requestListener, throwable.getMessage())
                        ));
            }

            @Override
            public void unrecommend(ItemId itemId, RecommendationView.RequestListener requestListener) {
                getSubscription().add(getRecommendationDataSource().removeItem(itemId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> mainActivity.onRecommendationRequestSuccess(response, requestListener,
                                        "Due to an error request to stop recommending is failed"),
                                throwable -> mainActivity.onRecommendationRequestFail(requestListener, throwable.getMessage())
                        ));
            }
        };
    }
}