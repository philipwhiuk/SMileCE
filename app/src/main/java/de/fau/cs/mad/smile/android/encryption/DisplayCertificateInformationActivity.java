package de.fau.cs.mad.smile.android.encryption;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisplayCertificateInformationActivity extends ActionBarActivity {
    private Toolbar toolbar;
    private String name;
    private String alias;
    private KeyInfo keyInfo;
    private HashMap<String, List<AbstractCertificateInfoItem>> listDataChild;
    private List<String> listDataHeader;
    ExpandableCertificateListAdapter listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(SMileCrypto.LOG_TAG, "Started DisplayCertificateInformationActivity.");
        Bundle extras = getIntent().getExtras();
        this.alias = extras.getString("Alias");
        if(this.alias == null) {
            Log.e(SMileCrypto.LOG_TAG, "Called without alias.");
            finish();
        }
        Log.d(SMileCrypto.LOG_TAG, "Called with alias: " + alias);
        this.name = extras.getString("Name");

        setContentView(R.layout.activity_display_certificate_information);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(this.name); //if (name == null) --> set later
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.lvExp);

        // preparing list data
        getKeyInfo();

        listAdapter = new ExpandableCertificateListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_display_certificate_information, menu);

        MenuItem item = menu.findItem(R.id.action_delete);
        if(item == null)
            return true;

        item.setActionView(R.layout.item_delete);
        RelativeLayout r = (RelativeLayout) item.getActionView();
        TextView textView = (TextView) r.findViewById(R.id.actionbar_delete);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DisplayCertificateInformationActivity.this, "Delete certificate…", Toast.LENGTH_SHORT).show();
                //TODO: delete
                deleteKey(keyInfo);
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.e(SMileCrypto.LOG_TAG, "item: " + id);
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if(id == R.id.action_delete) {
            Toast.makeText(this, "Delete certificate…", Toast.LENGTH_SHORT).show();
            deleteKey(this.keyInfo);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getKeyInfo() {
        try {
            KeyManagement keyManagement = new KeyManagement();
            KeyInfo keyInfo = keyManagement.getKeyInfo(this.alias);
            this.keyInfo = keyInfo;
            extractCertificateInformation(keyInfo);
        } catch (Exception e) {
            Log.e(SMileCrypto.LOG_TAG, "Error: " + e.getMessage());
            showErrorPrompt();
        }
    }

    private void extractCertificateInformation(KeyInfo keyInfo) {
        if(this.name == null) {
            this.name = keyInfo.contact;
            Log.d(SMileCrypto.LOG_TAG, "Name was null, set name to: " + this.name);
            toolbar.setTitle(this.name);
            setSupportActionBar(toolbar);
        }

        listDataHeader = new ArrayList<>();
        listDataHeader.add(getString(R.string.personal));
        listDataChild = new HashMap<>();
        HashMap<String, String> personalInfo = new HashMap<>();
        personalInfo.put("Name", keyInfo.contact);
        personalInfo.put("Email", keyInfo.mail);
        ArrayList<AbstractCertificateInfoItem> pers = new ArrayList<>();
        PersonalInformationItem personalInformationItem = new PersonalInformationItem();
        personalInformationItem.build(personalInfo);
        pers.add(personalInformationItem);
        listDataChild.put(listDataHeader.get(0), pers);

        listDataHeader.add(getString(R.string.validity));
        HashMap<String, String> validity = new HashMap<>();
        validity.put("Startdate", keyInfo.valid_after.toString());
        validity.put("Enddate", keyInfo.termination_date.toString());
        ArrayList<AbstractCertificateInfoItem> val = new ArrayList<>();
        ValidityItem validityItem  = new ValidityItem();
        validityItem.build(validity);
        val.add(validityItem);
        listDataChild.put(listDataHeader.get(1), val);

        listDataHeader.add(getString(R.string.certificate));
        HashMap<String, String> certificateInfo = new HashMap<>();
        certificateInfo.put("Thumbprint", keyInfo.thumbprint);
        BigInteger serialNumber = keyInfo.certificate.getSerialNumber();
        certificateInfo.put("Serial number", serialNumber.toString(16));
        certificateInfo.put("Version", Integer.toString(keyInfo.certificate.getVersion()));
        ArrayList<AbstractCertificateInfoItem> cert = new ArrayList<>();
        CertificateInformationItem certificateInformationItem = new CertificateInformationItem();
        certificateInformationItem.build(certificateInfo);
        cert.add(certificateInformationItem);
        listDataChild.put(listDataHeader.get(2), cert);

        listDataHeader.add(getString(R.string.cryptographic));
        HashMap<String, String> cryptographicInfo = new HashMap<>();
        PublicKey publicKey = keyInfo.certificate.getPublicKey();
        if(publicKey instanceof  RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            String modulus = rsaPublicKey.getModulus().toString(16);
            String exponent = rsaPublicKey.getPublicExponent().toString(16);
            cryptographicInfo.put("Public Key", "RSAPublicKey");
            cryptographicInfo.put("Modulus", modulus);
            cryptographicInfo.put("Exponent", exponent);
            cryptographicInfo.put("Signature Algorithm", keyInfo.certificate.getSigAlgName());
            cryptographicInfo.put("Signature", keyInfo.certificate.getSigAlgOID());
        } else {
            Log.d(SMileCrypto.LOG_TAG, "Not an instance of RSAPublicKey.");
            cryptographicInfo.put("Public Key", keyInfo.certificate.getPublicKey().toString());
            cryptographicInfo.put("Signature Algorithm", keyInfo.certificate.getSigAlgName());
            cryptographicInfo.put("Signature", new String(keyInfo.certificate.getSignature()));
        }
        ArrayList<AbstractCertificateInfoItem> crypto = new ArrayList<>();
        CryptographicInformationItem cryptographicInformationItem = new CryptographicInformationItem();
        cryptographicInformationItem.build(cryptographicInfo);
        crypto.add(cryptographicInformationItem);
        listDataChild.put(listDataHeader.get(3), crypto);

        /*
        * · Personal: Name, Email, SubjectDN
        · Validity: start date, end date
        · Certificate: Version, Serial Number, Thumbprint
        · Issuer: IssuerDN (splitted)
        · Cryptographic: Public Key, Signature algo, Signature */
    }

    private void deleteKey(final KeyInfo keyInfo) {
        final KeyManagement keyManagement;
        try {
            keyManagement = new KeyManagement();
        } catch (Exception e) {
            showErrorPrompt();
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        if (keyInfo.alias.startsWith("SMile_crypto_own")) {
            alertDialogBuilder.setTitle(getString(R.string.alert_header_start) + keyInfo.contact + getString(R.string.alert_header_end));
            alertDialogBuilder
                    .setMessage(getString(R.string.alert_content))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // if this button is clicked, close
                            // current activity
                            keyManagement.deleteKey(keyInfo.alias);
                            Toast.makeText(DisplayCertificateInformationActivity.this, R.string.delete, Toast.LENGTH_SHORT);
                            //TODO
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

           alertDialogBuilder.create().show();
        } else {
            // set dialog message
            alertDialogBuilder
                    .setMessage(getString(R.string.alert_header_start) + keyInfo.contact + getString(R.string.alert_header_end))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.erase), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            keyManagement.deleteKey(keyInfo.alias);
                            Toast.makeText(DisplayCertificateInformationActivity.this, R.string.delete, Toast.LENGTH_SHORT);
                            //TODO
                            finish();
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            alertDialogBuilder.create().show();
        }
    }

    private void showErrorPrompt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DisplayCertificateInformationActivity.this);
        builder.setTitle(getResources().getString(R.string.error));
        Log.e(SMileCrypto.LOG_TAG, "EXIT_STATUS: " + SMileCrypto.EXIT_STATUS);
        builder.setMessage(getResources().getString(R.string.internal_error));
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        builder.create().show();
    }
}