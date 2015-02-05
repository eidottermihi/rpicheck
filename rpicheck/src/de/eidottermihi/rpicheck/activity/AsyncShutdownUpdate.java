package de.eidottermihi.rpicheck.activity;

import de.eidottermihi.rpicheck.beans.ShutdownResult;

public interface AsyncShutdownUpdate {
	void onShutdownFinished(ShutdownResult result);
}
