package com.onelleloan.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    DB db; LinearLayout root, list; TextView summary; EditText nameInput, phoneInput, principalInput, rateInput, durationInput, dueInput;
    Spinner frequencySpinner;
    final int GREEN=Color.rgb(22,75,53), LIGHT=Color.rgb(239,246,241), MUTED=Color.rgb(99,115,105);
    NumberFormat currency=NumberFormat.getCurrencyInstance(new Locale("en","PH"));

    @Override public void onCreate(Bundle b){super.onCreate(b); db=new DB(this); buildUI(); render();}
    GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    LinearLayout vertical(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    LinearLayout horizontal(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    void pad(View v,int p){v.setPadding(p,p,p,p);}
    EditText field(String hint,int inputType){EditText e=new EditText(this);e.setSingleLine(true);e.setTextSize(15);e.setHint(hint);e.setInputType(inputType);e.setPadding(12,7,12,7);e.setBackground(bg(Color.WHITE,18));return e;}
    Button button(String label, boolean primary){Button b=new Button(this);b.setText(label);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(primary?Color.WHITE:GREEN);b.setBackground(bg(primary?GREEN:Color.rgb(226,238,230),22));return b;}
    void buildUI(){
        ScrollView scroll=new ScrollView(this); root=vertical(); root.setBackgroundColor(LIGHT); scroll.addView(root); setContentView(scroll);
        LinearLayout header=vertical();header.setPadding(20,24,20,22);header.setBackgroundColor(GREEN);
        header.addView(text("ONELL ELOAN",25,Color.WHITE,true));header.addView(text("Offline Loan & Payment Manager",14,Color.rgb(219,237,225),false));root.addView(header);
        LinearLayout content=vertical();content.setPadding(14,14,14,26);root.addView(content);
        summary=text("",14,Color.DKGRAY,false);summary.setPadding(15,15,15,15);summary.setBackground(bg(Color.WHITE,18));content.addView(summary);
        TextView formTitle=text("Add New Loan",20,GREEN,true); LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.setMargins(0,18,0,8);content.addView(formTitle,tp);
        LinearLayout form=vertical();form.setPadding(14,12,14,14);form.setBackground(bg(Color.WHITE,18));content.addView(form);
        nameInput=field("Borrower's full name", android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS); addField(form,"Borrower name",nameInput);
        phoneInput=field("Phone number (optional)",android.text.InputType.TYPE_CLASS_PHONE);addField(form,"Phone number",phoneInput);
        principalInput=field("e.g. 10000",android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);addField(form,"Principal amount (PHP)",principalInput);
        rateInput=field("e.g. 5",android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);addField(form,"Interest rate (%) per period",rateInput);
        frequencySpinner=new Spinner(this);String[] freq={"Monthly","Weekly"};frequencySpinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,freq));addField(form,"Interest period",frequencySpinner);
        durationInput=field("Number of periods",android.text.InputType.TYPE_CLASS_NUMBER);addField(form,"Loan duration (weeks/months)",durationInput);
        dueInput=field("YYYY-MM-DD (optional)",android.text.InputType.TYPE_CLASS_TEXT);addField(form,"First due date",dueInput);
        TextView calc=text("Enter the loan details to calculate interest and total due.",13,MUTED,false);calc.setPadding(0,8,0,8);form.addView(calc);
        Button add=button("＋  Save Loan",true);form.addView(add,new LinearLayout.LayoutParams(-1,-2));
        add.setOnClickListener(v->{
            String nm=nameInput.getText().toString().trim(); double p=number(principalInput),r=number(rateInput); int n=(int)number(durationInput);
            if(nm.isEmpty()||p<=0||r<0||n<=0){new AlertDialog.Builder(this).setMessage("Enter borrower name, principal above zero, a valid rate, and duration above zero.").setPositiveButton("OK",null).show();return;}
            String f=frequencySpinner.getSelectedItem().toString().toLowerCase(Locale.ROOT);
            double interest=p*(r/100.0)*n;
            db.addLoan(nm,phoneInput.getText().toString().trim(),p,r,f,n,dueInput.getText().toString().trim(),interest,p+interest);
            nameInput.setText("");phoneInput.setText("");principalInput.setText("");rateInput.setText("");durationInput.setText("");dueInput.setText("");
            ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(add.getWindowToken(),0);render();
        });
        TextView recTitle=text("Borrower Records",20,GREEN,true);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.setMargins(0,20,0,8);content.addView(recTitle,rp);
        list=vertical();content.addView(list);
        TextView note=text("Privacy: records stay on this phone in a local database. They are not synced online. Export or back up your records before changing phones or uninstalling.",12,MUTED,false);note.setPadding(4,16,4,0);content.addView(note);
    }
    void addField(LinearLayout parent,String label,View field){TextView l=text(label,12,MUTED,true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,8,0,4);parent.addView(l,lp);parent.addView(field,new LinearLayout.LayoutParams(-1,-2));}
    double number(EditText e){try{return Double.parseDouble(e.getText().toString().trim());}catch(Exception ex){return 0;}}
    String money(double x){return currency.format(x);}
    void render(){
        Cursor totals=db.getReadableDatabase().rawQuery("SELECT COUNT(*), COALESCE(SUM(principal),0), COALESCE(SUM(total),0) FROM loans",null); totals.moveToFirst();int count=totals.getInt(0);double principal=totals.getDouble(1),total=totals.getDouble(2);totals.close();
        Cursor paidCur=db.getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM payments",null);paidCur.moveToFirst();double collected=paidCur.getDouble(0);paidCur.close();
        summary.setText("DASHBOARD\nBorrowers: "+count+"     Principal: "+money(principal)+"\nCollected: "+money(collected)+"\nOutstanding: "+money(Math.max(0,total-collected)));
        list.removeAllViews();Cursor c=db.getReadableDatabase().rawQuery("SELECT id,name,phone,principal,rate,frequency,duration,due_date,interest,total FROM loans ORDER BY id DESC",null);
        if(!c.moveToFirst()){TextView empty=text("No loans yet. Add your first borrower above.",14,MUTED,false);empty.setPadding(14,22,14,22);empty.setBackground(bg(Color.WHITE,18));list.addView(empty);c.close();return;}
        do{final int id=c.getInt(0);String nm=c.getString(1),phone=c.getString(2),freq=c.getString(5),due=c.getString(7);double p=c.getDouble(3),rate=c.getDouble(4),interest=c.getDouble(8),tot=c.getDouble(9);int dur=c.getInt(6);double paid=db.totalPaid(id),balance=Math.max(0,tot-paid);
            LinearLayout card=vertical();card.setPadding(15,15,15,15);card.setBackground(bg(Color.WHITE,18));LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2);cp.setMargins(0,0,0,12);list.addView(card,cp);
            LinearLayout top=horizontal();TextView title=text(nm,18,GREEN,true);top.addView(title,new LinearLayout.LayoutParams(0,-2,1));TextView status=text(balance<=0?"PAID":"ACTIVE",11,balance<=0?Color.rgb(22,112,57):Color.rgb(150,94,0),true);top.addView(status);card.addView(top);
            if(phone!=null&&!phone.isEmpty())card.addView(text(phone,12,MUTED,false));
            String weekly=freq.equals("weekly")?money(p*rate/100):money(p*rate/400);
            String monthly=freq.equals("monthly")?money(p*rate/100):money(p*rate*4/100);
            TextView details=text("Principal: "+money(p)+"\nRate: "+rate+"% per "+freq+" · Duration: "+dur+" periods\nWeekly interest: "+weekly+"   Monthly interest: "+monthly+"\nTotal interest: "+money(interest)+"\nTotal due: "+money(tot)+(due==null||due.isEmpty()?"":"\nFirst due date: "+due),13,Color.DKGRAY,false);details.setPadding(0,12,0,12);card.addView(details);
            LinearLayout balrow=horizontal();balrow.addView(text("Remaining balance",14,MUTED,false),new LinearLayout.LayoutParams(0,-2,1));balrow.addView(text(money(balance),17,GREEN,true));card.addView(balrow);
            LinearLayout actions=horizontal();Button pay=button("＋ Record Payment",true),history=button("Payment History",false),del=button("Delete",false);actions.addView(pay,new LinearLayout.LayoutParams(0,-2,1));actions.addView(history,new LinearLayout.LayoutParams(0,-2,1));actions.addView(del,new LinearLayout.LayoutParams(0,-2,1));card.addView(actions);
            pay.setOnClickListener(v->paymentDialog(id,nm,balance));history.setOnClickListener(v->historyDialog(id,nm));del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Delete loan?").setMessage("Delete "+nm+" and its payment history?").setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{db.deleteLoan(id);render();}).show());
        }while(c.moveToNext());c.close();
    }
    void paymentDialog(int id,String name,double balance){
        LinearLayout box=vertical();box.setPadding(18,6,18,0);EditText amount=field("Payment amount",android.text.InputType.TYPE_CLASS_NUMBER|android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText date=field("YYYY-MM-DD (optional)",android.text.InputType.TYPE_CLASS_TEXT);EditText note=field("Note (optional)",android.text.InputType.TYPE_CLASS_TEXT);addField(box,"Amount (balance: "+money(balance)+")",amount);addField(box,"Payment date",date);addField(box,"Note",note);
        new AlertDialog.Builder(this).setTitle("Record payment · "+name).setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save Payment",(d,w)->{double a=number(amount);if(a<=0){Toast.makeText(this,"Enter a payment greater than zero",Toast.LENGTH_SHORT).show();return;}db.addPayment(id,a,date.getText().toString().trim(),note.getText().toString().trim());render();}).show();
    }
    void historyDialog(int id,String name){
        Cursor c=db.getReadableDatabase().rawQuery("SELECT amount,payment_date,note FROM payments WHERE loan_id=? ORDER BY id DESC",new String[]{String.valueOf(id)});
        StringBuilder s=new StringBuilder();double total=0;if(c.moveToFirst()){do{double a=c.getDouble(0);total+=a;s.append(money(a)).append(" · ").append(c.getString(1)==null?"":c.getString(1));if(c.getString(2)!=null&&!c.getString(2).isEmpty())s.append("\n").append(c.getString(2));s.append("\n\n");}while(c.moveToNext());}else s.append("No payments recorded yet.");c.close();
        new AlertDialog.Builder(this).setTitle("Payment History · "+name).setMessage(s.toString()+"\nTotal recorded: "+money(total)).setPositiveButton("Close",null).show();
    }
    static class DB extends SQLiteOpenHelper{
        DB(Context c){super(c,"onell_eLoan.db",null,1);}
        public void onCreate(SQLiteDatabase d){d.execSQL("CREATE TABLE loans(id INTEGER PRIMARY KEY AUTOINCREMENT,name TEXT NOT NULL,phone TEXT,principal REAL,rate REAL,frequency TEXT,duration INTEGER,due_date TEXT,interest REAL,total REAL)");d.execSQL("CREATE TABLE payments(id INTEGER PRIMARY KEY AUTOINCREMENT,loan_id INTEGER,amount REAL,payment_date TEXT,note TEXT,FOREIGN KEY(loan_id) REFERENCES loans(id))");}
        public void onUpgrade(SQLiteDatabase d,int oldV,int newV){}
        void addLoan(String name,String phone,double p,double rate,String f,int duration,String due,double interest,double total){ContentValues v=new ContentValues();v.put("name",name);v.put("phone",phone);v.put("principal",p);v.put("rate",rate);v.put("frequency",f);v.put("duration",duration);v.put("due_date",due);v.put("interest",interest);v.put("total",total);getWritableDatabase().insert("loans",null,v);}
        void addPayment(int loan,double amount,String date,String note){ContentValues v=new ContentValues();v.put("loan_id",loan);v.put("amount",amount);v.put("payment_date",date);v.put("note",note);getWritableDatabase().insert("payments",null,v);}
        double totalPaid(int id){Cursor c=getReadableDatabase().rawQuery("SELECT COALESCE(SUM(amount),0) FROM payments WHERE loan_id=?",new String[]{String.valueOf(id)});c.moveToFirst();double x=c.getDouble(0);c.close();return x;}
        void deleteLoan(int id){SQLiteDatabase d=getWritableDatabase();d.delete("payments","loan_id=?",new String[]{String.valueOf(id)});d.delete("loans","id=?",new String[]{String.valueOf(id)});}
    }
}
