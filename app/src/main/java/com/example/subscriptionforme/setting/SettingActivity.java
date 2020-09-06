package com.example.subscriptionforme.setting;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import com.example.subscriptionforme.R;
import com.example.subscriptionforme.home.Data.AccountDatabase;
import com.example.subscriptionforme.home.Data.SubscriptionDatabase;
import com.example.subscriptionforme.home.Data.UserDatabase;
import com.example.subscriptionforme.home.Data.UserSubscriptionData;
import com.example.subscriptionforme.recommendation.RecommendationList;
import com.example.subscriptionforme.setting.card.AccountVO;
import com.example.subscriptionforme.setting.card.CardActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class SettingActivity extends AppCompatActivity {

    Button cardDataButton;    // 카드 정보 읽기 버튼
    Button cardCsvDataButton; // 카드 csv 정보 받기 버튼
    Button subscriptionDataButton; // 내 구독 관리, 추천 csv 정보 받기 버튼
    Button surveyButton; //설문조사 버튼
    int dataCount;
    int userDataCount;
    int accountDataCount;

    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        dataCount = 0;
        userDataCount = 0;
        accountDataCount = 0;
        setContentView(R.layout.activity_detail_setting);
    }

    @Override
    public void onStart() {

        super.onStart();
        cardDataButton = findViewById(R.id.buttonCardData);
        cardCsvDataButton = findViewById(R.id.buttonCardDataCSV);
        subscriptionDataButton = findViewById(R.id.buttonDataCSV);
        surveyButton = findViewById(R.id.buttonSurvey);

        cardDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(view.getContext(), CardActivity.class);
                startActivity(intent);
            }
        });

        cardCsvDataButton.setOnClickListener(new View.OnClickListener() {



            @Override
            public void onClick(View view) {

                SQLiteDatabase accountDatabase = AccountDatabase.getInstance(getApplicationContext()).getReadableDatabase();
                ArrayList<AccountVO> accountList = new ArrayList<AccountVO>();
                accountDataCount = AccountDatabase.getInstance(getApplicationContext()).getDataCount(AccountDatabase.getInstance(getApplicationContext()).getReadableDatabase());

                for(int i=0;i<accountDataCount;i++){
                    accountList.add(AccountDatabase.getInstance(getApplicationContext()).getAccountData(accountDatabase, i));
                }

                StringBuilder data = new StringBuilder();
                data.append("결제 날짜,결제 시간,출금 금액,입금 금액,입금자,예금자,통장 잔고");


                for(int i=0;i<accountDataCount;i++){
                    data.append("\n"+accountList.get(i).getResAccountTrDate()+","+accountList.get(i).getResAccountTrTime()+","+accountList.get(i).getResAccountOut()+","+accountList.get(i).getResAccountIn()+","+accountList.get(i).getResAccountDesc1()+","+accountList.get(i).getResAccountDesc3()+","+accountList.get(i).getResAfterTranBalance());
                }

                try {
                    FileOutputStream out = openFileOutput("carddata.csv", Context.MODE_PRIVATE);
                    out.write((data.toString()).getBytes());
                    out.close();

                    Context context = getApplicationContext();
                    File filelocation = new File(getFilesDir(), "carddata.csv");
                    Uri path = FileProvider.getUriForFile(context, "com.example.subscriptionforme.setting.fileprovider",filelocation);
                    Intent fileIntent =  new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/csv");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent, "Send mail"));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        subscriptionDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder data = new StringBuilder();
                ArrayList<RecommendationList> recommendationLists = new ArrayList<RecommendationList>();
                ArrayList<UserSubscriptionData> userSubscriptionData = new ArrayList<UserSubscriptionData>();
                SQLiteDatabase subscriptionDatabase = SubscriptionDatabase.getInstance(getApplicationContext()).getReadableDatabase();
                SQLiteDatabase userDatabase = UserDatabase.getInstance(getApplicationContext()).getReadableDatabase();
                dataCount = SubscriptionDatabase.getInstance(getApplicationContext()).getDataCount(SubscriptionDatabase.getInstance(getApplicationContext()).getReadableDatabase());
                userDataCount = UserDatabase.getInstance(getApplicationContext()).getDataCount(UserDatabase.getInstance(getApplicationContext()).getReadableDatabase());

                for(int i =0; i<dataCount;i++){
                    recommendationLists.add(SubscriptionDatabase.getInstance(getApplicationContext()).getSubscriptionData(subscriptionDatabase, i));
                }

                for(int i =0; i<userDataCount;i++){
                    userSubscriptionData.add(UserDatabase.getInstance(getApplicationContext()).getUserData(userDatabase, i));
                }

                Log.d("태순", String.valueOf(recommendationLists.get(0).getPrice()));
                Log.d("태순", String.valueOf(recommendationLists.get(0).getDiscount()));
                Log.d("태순", String.valueOf(recommendationLists.get(0).getConsumption()));


                data.append("paymentDay,resMemberStoreName,resPaymentAmt,useMoney,maxSale,manageMent,recommend");

                for(int i=0;i<dataCount;i++){
                    data.append("\n"+'X'+","+recommendationLists.get(i).getName()+","+recommendationLists.get(i).getPrice().replace(",","")+","+recommendationLists.get(i).getConsumption().replace(",","")+","+recommendationLists.get(i).getDiscount().replace(",","")+"," + 'X' + "," + 'O');
                    //data.append("\n"+accountList.get(i).getResAccountTrDate()+","+accountList.get(i).getResAccountTrTime()+","+accountList.get(i).getResAccountOut()+","+accountList.get(i).getResAccountIn()+","+accountList.get(i).getResAccountDesc1()+","+accountList.get(i).getResAccountDesc3()+","+accountList.get(i).getResAfterTranBalance());
                }

                for(int i=0;i<userDataCount;i++){
                    data.append("\n"+userSubscriptionData.get(i).getBeginningPayDate()+","+userSubscriptionData.get(i).getSubscriptionName()+","+userSubscriptionData.get(i).getSubscriptionPrice().replace(",","")+","+"X"+","+"X"+","+"O"+","+"X");
                }

                try {
                    FileOutputStream out = openFileOutput("subcriptionData.csv", Context.MODE_PRIVATE);
                    out.write((data.toString()).getBytes());
                    out.close();

                    Context context = getApplicationContext();
                    File filelocation = new File(getFilesDir(), "subcriptionData.csv");
                    Uri path = FileProvider.getUriForFile(context, "com.example.subscriptionforme.setting.fileprovider",filelocation);
                    Intent fileIntent =  new Intent(Intent.ACTION_SEND);
                    fileIntent.setType("text/csv");
                    fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Data");
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                    startActivity(Intent.createChooser(fileIntent, "Send mail"));

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        surveyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), SurveyActivity.class);
                startActivity(intent);
            }
        });

    }
}
