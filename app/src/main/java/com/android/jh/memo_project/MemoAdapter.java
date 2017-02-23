package com.android.jh.memo_project;

import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.jh.memo_project.domain.Memo;
import com.android.jh.memo_project.interfaces.AdapterInterface;
import com.bumptech.glide.Glide;

import java.sql.SQLException;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder> {

    Context context;
    List<Memo> datas;
    View view;
    AlertDialog dialog;
    AdapterInterface adapterInterface;


    public MemoAdapter(Context context, List<Memo> datas) {
        this.context = context;
        this.datas = datas;
        this.adapterInterface = (AdapterInterface)context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
         view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.memo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.tv_content.setText(datas.get(position).getMemo());
        for(int i = 0; i< datas.size(); i++) {
            Log.i("MAIN", "0--------------------------------------" + datas.get(i).getMemo());
        }
        holder.position = position;
        holder.tv_listdate.setText(datas.get(position).getDate().toString());
        if(datas.get(position).getImgUri()!=null) {
            holder.list_img.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(Uri.parse(datas.get(position).getImgUri()))
                    .into(holder.list_img);
        } else {
            holder.list_img.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_content;
        public TextView tv_listdate;
        public CardView cardView;
        public ImageView list_img;
        public ImageButton btn_delete;
        public int position;
        public ViewHolder(View view) {
            super(view);
            tv_content = (TextView) view.findViewById(R.id.textView);
            tv_listdate = (TextView) view.findViewById(R.id.tv_listdate);
            list_img = (ImageView) view.findViewById(R.id.list_img);
            cardView = (CardView) view.findViewById(R.id.cardView);
            btn_delete = (ImageButton) view.findViewById(R.id.btn_delete);
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btn_delete.setVisibility(View.GONE);
                    listAlert(position);
                }
            });
            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    btn_delete.setVisibility(View.VISIBLE);
                    return true;
                }
            });
            btn_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        adapterInterface.delete(position);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void listAlert(int position) {
        // LayoutInflater를 통해 위의 custom layout을 AlertDialog에 반영. 이 외에는 거의 동일하다.
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view3 = inflater.inflate(R.layout.detailalert,null);

        //멤버의 세부내역 입력 Dialog 생성 및 보이기
        final AlertDialog.Builder buider = new AlertDialog.Builder(context); //AlertDialog.Builder 객체 생성
        TextView tv_content = (TextView)view3.findViewById(R.id.tv_content);
        TextView tv_date = (TextView)view3.findViewById(R.id.date);
        final ImageView show_img = (ImageView) view3.findViewById(R.id.show_img);

        if(datas.get(position).getImgUri()!=null) {
            show_img.setVisibility(View.VISIBLE);
            Uri uri = Uri.parse(datas.get(position).getImgUri());
            Glide.with(context)
                    .load(uri)
                    .into(show_img);
        }

        Button btn_ok = (Button)view3.findViewById(R.id.btn_ok);
        tv_content.setText(datas.get(position).getMemo());
        tv_date.setText(datas.get(position).getDate().toString());
        buider.setView(view3);
        dialog = buider.create();

        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                show_img.setVisibility(View.GONE);
            }
        });
        dialog.show();
    }
}
