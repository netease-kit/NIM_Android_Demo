package com.netease.nim.demo.main.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.netease.nim.demo.R;
import com.netease.nim.demo.config.preference.Preferences;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nimlib.push.net.lbs.IPVersion;
import com.netease.nimlib.push.packet.asymmetric.AsymmetricType;
import com.netease.nimlib.push.packet.symmetry.SymmetryType;
import com.netease.nimlib.sdk.NimHandshakeType;

public class PublicConfigActivity extends AppCompatActivity implements View.OnClickListener {
    private RadioGroup handShakeRg;
    private RadioGroup asymmetricRg;
    private RadioGroup symmetryRg;
    private RadioGroup ipvRg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_config);
        initViews();
    }

    private void initViews() {
        handShakeRg = findViewById(R.id.rg_hand_shake);
        asymmetricRg = findViewById(R.id.rg_asymmetric);
        symmetryRg = findViewById(R.id.rg_symmetry);
        ipvRg = findViewById(R.id.rg_ipv);
        initChecked();

        findViewById(R.id.btn_ok).setOnClickListener(this);
    }

    private void initChecked(){
        NimHandshakeType handshakeType = Preferences.getHandshakeType();
        AsymmetricType asymmetric = Preferences.getAsymmetric();
        SymmetryType symmetry = Preferences.getSymmetry();
        IPVersion ipv = Preferences.getIpv();

        switch (handshakeType) {
            case V0:
                handShakeRg.check(R.id.rb_hand_shake_type_5);
                break;
            case V1:
                handShakeRg.check(R.id.rb_hand_shake_type_1);
                break;
            default:
                break;
        }

        switch (asymmetric) {
            case RSA:
                asymmetricRg.check(R.id.rb_asymmetric_rsa);
                break;
            case SM2:
                asymmetricRg.check(R.id.rb_asymmetric_sm2);
                break;
            case RSA_OAEP_1:
                asymmetricRg.check(R.id.rb_asymmetric_rsa_oaep_1);
                break;
            case RSA_OAEP_256:
                asymmetricRg.check(R.id.rb_asymmetric_rsa_oaep_256);
                break;
            default:
                break;
        }

        switch (symmetry) {
            case RC4:
                symmetryRg.check(R.id.rb_symmetry_rc4);
                break;
            case AES:
                symmetryRg.check(R.id.rb_symmetry_aes);
                break;
            case SM4:
                symmetryRg.check(R.id.rb_symmetry_sm4);
                break;
            default:
                break;
        }

        switch (ipv) {
            case IPV4:
                ipvRg.check(R.id.rb_ipv4);
                break;
            case IPV6:
                ipvRg.check(R.id.rb_ipv6);
                break;
            case ANY:
                ipvRg.check(R.id.rb_ipv_auto);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:
                onConfirmSelection();
                break;
            default:
                break;
        }
    }

    private void onConfirmSelection() {
        final int checkedHandshakeId = handShakeRg.getCheckedRadioButtonId();
        final int checkedAsymmetricId = asymmetricRg.getCheckedRadioButtonId();
        final int checkedSymmetryId = symmetryRg.getCheckedRadioButtonId();
        final int checkedIpvId = ipvRg.getCheckedRadioButtonId();

        switch (checkedHandshakeId) {
            case R.id.rb_hand_shake_type_1:
                Preferences.saveHandshakeType(NimHandshakeType.V1);
                break;
            case R.id.rb_hand_shake_type_5:
                Preferences.saveHandshakeType(NimHandshakeType.V0);
                break;
            default:
                break;
        }

        switch (checkedAsymmetricId) {
            case R.id.rb_asymmetric_rsa:
                Preferences.saveAsymmetric(AsymmetricType.RSA);
                break;
            case R.id.rb_asymmetric_sm2:
                Preferences.saveAsymmetric(AsymmetricType.SM2);
                break;
            case R.id.rb_asymmetric_rsa_oaep_1:
                Preferences.saveAsymmetric(AsymmetricType.RSA_OAEP_1);
                break;
            case R.id.rb_asymmetric_rsa_oaep_256:
                Preferences.saveAsymmetric(AsymmetricType.RSA_OAEP_256);
                break;
            default:
                break;
        }

        switch (checkedSymmetryId) {
            case R.id.rb_symmetry_rc4:
                Preferences.saveSymmetry(SymmetryType.RC4);
                break;
            case R.id.rb_symmetry_aes:
                Preferences.saveSymmetry(SymmetryType.AES);
                break;
            case R.id.rb_symmetry_sm4:
                Preferences.saveSymmetry(SymmetryType.SM4);
                break;
            default:
                break;
        }

        switch (checkedIpvId) {
            case R.id.rb_ipv4:
                Preferences.saveIpv(IPVersion.IPV4);
                break;
            case R.id.rb_ipv6:
                Preferences.saveIpv(IPVersion.IPV6);
                break;
            case R.id.rb_ipv_auto:
                Preferences.saveIpv(IPVersion.ANY);
                break;
            default:
                break;
        }

        ToastHelper.showToast(this.getApplicationContext(), getString(R.string.effective_after_restart));
    }
}
