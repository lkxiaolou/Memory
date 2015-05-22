package com.lkxiaolou.shorthand;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity {

    //m*n个格子
    int m = 2;
    int n = 2;

    int win = 0;//已经正确的格子数量
    int score_num = 0;
    int level = 1;
    int ms = 3000;//翻开的时间间隔

    public TableLayout layout;
    public Context context;
    public TextView score;
    public TableRow scoreRow;

    //记录随机产生的几个格子
    public ArrayList randomList;
    public ArrayList cardList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.layout = (TableLayout)findViewById(R.id.gameui);
        this.context = this.layout.getContext();
        this.setScore(true);
        this.start();
    }

    //开始一局游戏
    public void start()
    {
        this.randomList = new ArrayList();
        this.cardList = new ArrayList();
        win = 0;
        //获取随机数
        this.getRandomList(this.n);
        //绘制
        this.CreateView();
        //随机出现:开个新线程做
        this.showRandCard(this.n);
        //暂停3秒
        final Handler handler = new hideCardHandle();
        //定时器
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        Timer timer = new Timer(true);
        timer.schedule(task, ms);
    }

    //隐藏的线程定时
    class hideCardHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            //盖住
            for(int i=0;i<n;i++){
                Card card = (Card)cardList.get((Integer)randomList.get(i));
                card.btn.setImageResource(R.drawable.a);
                card.btn.setClickable(true);
                Log.i("lk+hide", ""+i);
            }
            super.handleMessage(msg);
        }
    }

    //过关时的线程定时
    class levelupHandle extends Handler{
        @Override
        public void handleMessage(Message msg) {
            start();
            super.handleMessage(msg);
        }
    }

    //绘制主界面
    public void CreateView()
    {
        //清除布局上的东西
        this.layout.removeAllViews();
        this.setScore(true);
        int counter = 0;
        cardList = new ArrayList();
        for(int i = 0; i< this.m; i++){
            TableRow tb = new TableRow(this.context);
            tb.setHorizontalGravity(Gravity.CENTER);
            for(int j = 0; j < this.n; j++){
                Log.i("lk+paint", i+""+j);
                Card card = new Card(counter);
                cardList.add(counter, card);
                tb.addView(card.btn);
                counter++;
            }
            this.layout.addView(tb);
        }
    }

    //设置得分
    public void setScore(boolean init)
    {
        if(init){//初始
            this.scoreRow = new TableRow(this.context);
            TextView txt = new TextView(this.context);
            txt.setText("得分：");
            txt.setMinimumHeight(80);
            this.score = new TextView(this.context);
            this.score.setText("" + score_num);
            this.score.setMinimumHeight(80);
            this.scoreRow.addView(txt);
            this.scoreRow.addView(this.score);
            this.layout.addView(this.scoreRow);
        }else{//设置
            this.score.setText("" + score_num);
        }
    }

    //产生不重复的随机数放进randomList
    public void getRandomList(int n)
    {
        this.randomList = new ArrayList();
        int num;
        int i = 0;
        while(i < n)
        {
            num = new Random().nextInt(this.m*this.n);
            Log.i("lk+random", ""+num);
            if(!this.randomList.contains(num)){
                this.randomList.add(i, num);
                i++;
            }
        }
    }

    //将随机的方格出现
    public void showRandCard(int n)
    {
        //第一次显示
        for(int i=0;i<n;i++){
            Card card = (Card)this.cardList.get((Integer) this.randomList.get(i));
            card.btn.setClickable(false);
            card.btn.setImageResource(R.drawable.b);
            Log.i("lk+show", ""+i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //方格类
     class Card {
        public ImageButton btn;
        public int index;

        public Card(int index){
            this.index = index;
            this.btn = new ImageButton(context);
            btn.setImageResource(R.drawable.a);
            //监听事件
            btn.setOnClickListener(new ButtonListener());
        }
        //点击类
        class ButtonListener implements View.OnClickListener{
            @Override
            public void onClick(View view) {
                ImageButton v = ((ImageButton)view);
                //获取当前随机的button
                ArrayList seleced = new ArrayList();
                for(int i = 0;i<n;i++){
                    Card c = (Card) cardList.get((Integer) randomList.get(i));
                    seleced.add(i, c.btn);
                }
                //正确
                if(seleced.contains((ImageButton)view)){
                    view.setClickable(false);
                    score_num++;
                    setScore(false);
                    win++;
                    v.setImageResource(R.drawable.b);
                    if(win == randomList.size()){
                        mnadd();
                        //暂停1秒
                        final Handler levelhandler = new levelupHandle();
                        //定时器
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                Message message = new Message();
                                message.what = 1;
                                levelhandler.sendMessage(message);
                            }
                        };
                        Timer timer = new Timer(true);
                        timer.schedule(task, 1000);
                        //start();
                    }
                }else{
                    v.setImageResource(R.drawable.c);
                    //错误
                    new AlertDialog.Builder(context).setTitle("GameOver").setMessage("得分"+score_num).setPositiveButton("再玩一次", new restart()).show();
                }
            }
        }
    }

    //重玩
    class restart implements DialogInterface.OnClickListener{
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            level = 1;
            score_num = 0;
            m=2;n=2;ms=3000;
            start();
        }
    }

    //m,n递增
    public void mnadd()
    {
        level++;
        switch(level)
        {
            case 1:
                m=2;n=2;ms=3000;break;
            case 2:
                m=2;n=3;ms=3000;break;
            case 3:
                m=3;n=3;ms=2000;break;
            case 4:
                m=3;n=4;ms=2000;break;
            case 5:
                m=4;n=4;ms=1000;break;
            default:
                m=4;n=4;ms=1000;
        }
    }
}
