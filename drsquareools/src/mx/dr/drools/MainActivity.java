package mx.dr.drools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mx.dr.drools.conf.DRKnowledgeBuilder;

import org.drools.KnowledgeBase;
import org.drools.builder.ResourceType;
import org.drools.runtime.StatelessKnowledgeSession;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private ProgressDialog _progressDialog;
	private boolean newknowledge=false;
	private KnowledgeBase base;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
			DRKnowledgeBuilder.assetsToFiles(this, "Files", "drl");
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	   
		/*List<Object> facts = new ArrayList<Object>();
        Account account= new Account();
        account.setBalance(60);
        facts.add(account);

		StatelessKnowledgeSession ksession=base.newStatelessKnowledgeSession();

		ksession.execute(facts);*/
		
		Button button= (Button) findViewById(R.id.fire);
		button.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				EditText t = (EditText) findViewById(R.id.balance);
				int val=0;
				try{
				    val=Integer.valueOf(t.getText().toString());
				}catch(Exception ex){
					Toast.makeText(MainActivity.this,
							R.string.error_number, Toast.LENGTH_SHORT).show();
					return;
				}
				showDialog(1);
				Account account= new Account();
				account.setBalance(val);
                new ButtonTask().execute(account);
			}
		});
		loadRule("rule1.drl");
		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				  RadioButton radioButton = (RadioButton) findViewById(arg1);
				  String rulename=radioButton.getText().toString().replaceFirst("Edit ", "");
				  writeRule(rulename.endsWith("1.drl")?"rule2.drl":"rule1.drl");
			      loadRule(rulename);
				
			}
		});
		/*btnDisplay = (Button) findViewById(R.id.btnDisplay);
	 
		btnDisplay.setOnClickListener(new OnClickListener() {
	 
			@Override
			public void onClick(View v) {
	 
			        // get selected radio button from radioGroup
				int selectedId = radioSexGroup.getCheckedRadioButtonId();
	 
				// find the radiobutton by returned id
			        radioSexButton = (RadioButton) findViewById(selectedId);
	 
				Toast.makeText(MyAndroidAppActivity.this,
					radioSexButton.getText(), Toast.LENGTH_SHORT).show();
	 
			}
	 
		});*/
	 

	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
    
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 1:
			_progressDialog = new ProgressDialog(this);
			_progressDialog.setIcon(R.drawable.ic_launcher);
			_progressDialog.setTitle(R.string.progress);
			_progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			return _progressDialog;
		}
		return null;
	}
	private  String readRuleFile(String name){
		try {
			FileInputStream io= openFileInput(name);
			BufferedReader reader= new BufferedReader(new InputStreamReader(io));
		    String s;
		    StringBuffer sf=new StringBuffer();
			while((s=reader.readLine())!=null){
		    	sf.append(s).append("\n");
		    	
		    }
			io.close();
			return sf.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}
	private  void loadRule(String name){
		EditText ruleEditor = (EditText) findViewById(R.id.rule_editor);
		String s=readRuleFile(name);
		ruleEditor.setText(s);
				
	}
	
	private void writeRule(String name){
		EditText ruleEditor = (EditText) findViewById(R.id.rule_editor);
		String s=ruleEditor.getText().toString();
		if(!s.equalsIgnoreCase(readRuleFile(name))){
			FileOutputStream  fOut;
			try {
				fOut = openFileOutput(name,Context.MODE_WORLD_READABLE);
				fOut.write(s.getBytes());
				fOut.close();
				newknowledge=true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private class ButtonTask extends AsyncTask<Account, Void, Account>{

		@Override
		protected Account doInBackground(Account... arg0) {
			RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
		    int selectedId = radioGroup.getCheckedRadioButtonId();
			RadioButton radioButton = (RadioButton) findViewById(selectedId);
			String rulename=radioButton.getText().toString().replaceFirst("Edit ", "");
			writeRule(rulename);
			List<Account> facts = new ArrayList<Account>();
	        	        facts.add(arg0[0]);
	        if (base==null||newknowledge){
	        	base=DRKnowledgeBuilder.loadKnowledge(MainActivity.this,
    					"/drools.properties",
    					"/mychange.xml",
    					ResourceType.CHANGE_SET);
	        	newknowledge=false;
	        }
	            
	        if(base==null){
	        	return arg0[0];
	        }
	        StatelessKnowledgeSession ksession=base.newStatelessKnowledgeSession();
			ksession.execute(facts);
			return arg0[0];
		}
		
		@Override
		protected void onPostExecute( Account result){
			TextView t2 = (TextView) findViewById(R.id.message);
			t2.setText(result.getMessage());
			_progressDialog.dismiss();
		}
	}
}
