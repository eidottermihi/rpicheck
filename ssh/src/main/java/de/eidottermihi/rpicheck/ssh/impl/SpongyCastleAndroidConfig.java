package de.eidottermihi.rpicheck.ssh.impl;

import net.schmizz.sshj.AndroidConfig;
import net.schmizz.sshj.Config;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.random.BouncyCastleRandom;
import net.schmizz.sshj.transport.random.SingletonRandomFactory;

public class SpongyCastleAndroidConfig extends AndroidConfig {
    public SpongyCastleAndroidConfig() {
        super();
        initKeyExchangeFactories(true);
        initRandomFactory(true);
        initFileKeyProviderFactories(true);
    }
}
