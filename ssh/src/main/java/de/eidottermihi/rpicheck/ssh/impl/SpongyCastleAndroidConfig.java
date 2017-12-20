package de.eidottermihi.rpicheck.ssh.impl;

import com.hierynomus.sshj.signature.SignatureEdDSA;

import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.signature.SignatureDSA;
import net.schmizz.sshj.signature.SignatureRSA;
import net.schmizz.sshj.transport.random.JCERandom;
import net.schmizz.sshj.transport.random.SingletonRandomFactory;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public class SpongyCastleAndroidConfig extends DefaultConfig {

    static {
        SecurityUtils.registerSecurityProvider("org.spongycastle.jce.provider.BouncyCastleProvider");
    }

    public SpongyCastleAndroidConfig() {
        super();
        initKeyExchangeFactories(true);
        initRandomFactory(true);
        initFileKeyProviderFactories(true);
    }

    // don't add ECDSA
    protected void initSignatureFactories() {
        setSignatureFactories(new SignatureRSA.Factory(), new SignatureDSA.Factory(),
                // but add EdDSA !
                new SignatureEdDSA.Factory());
    }

    @Override
    protected void initRandomFactory(boolean ignored) {
        setRandomFactory(new SingletonRandomFactory(new JCERandom.Factory()));
    }
}
