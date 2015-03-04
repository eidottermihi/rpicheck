package de.eidottermihi.rpicheck.activity;

import de.eidottermihi.rpicheck.beans.QueryBean;

public interface AsyncQueryDataUpdate {

	void onQueryFinished(QueryBean result);

	void onQueryProgress(int progress);

}
