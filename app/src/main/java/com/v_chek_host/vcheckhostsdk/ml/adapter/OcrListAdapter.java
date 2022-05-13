package com.v_chek_host.vcheckhostsdk.ml.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.v_chek_host.vcheckhostsdk.R;
import com.v_chek_host.vcheckhostsdk.VinNumberActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.v_chek_host.vcheckhostsdk.VinNumberActivity.vinFilter;

public class OcrListAdapter extends RecyclerView.Adapter<OcrListAdapter.MyViewHolder> {
    List<OcrList> ocrListList;
    Context context;
    int selectedItemPosition = -1;
   int activityId=0;
    public OcrListAdapter(List<OcrList> ocrListList, Context context,int activityId) {
        this.ocrListList = ocrListList;
        this.context = context;
        this.activityId = activityId;
    }

    @NonNull
    @Override
    public OcrListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ocr_list_item, parent, false);
        MyViewHolder vh = new MyViewHolder(itemView/*, new MyCustomEditTextListener()*/);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull OcrListAdapter.MyViewHolder holder, int position) {
        /*if (holder.textWatcher != null)
            holder.etOcr.removeTextChangedListener(holder.textWatcher);*/
        OcrList ocrList = ocrListList.get(position);
        holder.etOcr.setVisibility(View.GONE);
        holder.saveImage.setVisibility(View.GONE);
        holder.cancelImage.setVisibility(View.GONE);
        holder.textOcr.setVisibility(View.VISIBLE);


        if (ocrList.isSelected) {
            holder.rbtSelected.setChecked(true);
          //  if(activityId==1)
                holder.editImage.setVisibility(View.VISIBLE);
          /*  else
                holder.editImage.setVisibility(View.GONE);*/
           // holder.etOcr.setEnabled(true);
           // holder.saveImage.setVisibility(View.VISIBLE);
        } else {
            holder.rbtSelected.setChecked(false);
            holder.editImage.setVisibility(View.GONE);
           // holder.etOcr.setEnabled(false);
           // holder.saveImage.setVisibility(View.INVISIBLE);
        }

        /*holder.rbtSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    for (OcrList ocrList1 : ocrListList) {
                        ocrList1.setSelected(false);
                        notifyDataSetChanged();
                    }
                    ocrListList.get(position).setSelected(true);
                    notifyDataSetChanged();
                }
            }
        });*/
        //holder.rbtSelected.setOnClickListener(first_radio_listener);
       /* holder.etOcr.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.
                LengthFilter(VinNumberActivity.vinMaxLength), vinFilter});
        holder.myCustomEditTextListener.updatePosition(holder.getAdapterPosition());
        holder.etOcr.setText(ocrListList.get(holder.getAdapterPosition()).ocrText);*/
        holder.rbtSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("jkhahdasd");
                if (!ocrListList.get(position).isSelected()) {

                    for (int i = 0; i < ocrListList.size(); i++) {
                        ocrListList.get(i).setSelected(false);
                    }
                    //notifyDataSetChanged();
                    ocrList.setSelected(true);
                    notifyDataSetChanged();
                }
            }
        });
        holder.textOcr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("jkhahdasd");
                if (!ocrListList.get(position).isSelected()) {

                    for (int i = 0; i < ocrListList.size(); i++) {
                        ocrListList.get(i).setSelected(false);
                    }
                    //notifyDataSetChanged();
                    ocrList.setSelected(true);
                    notifyDataSetChanged();
                }
            }
        });
        holder.textOcr.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.
                LengthFilter(VinNumberActivity.vinMaxLength), vinFilter});
        holder.etOcr.setFilters(new InputFilter[]{new InputFilter.AllCaps(), new InputFilter.
                LengthFilter(VinNumberActivity.vinMaxLength), vinFilter});
        holder.textOcr.setText(ocrList.getOcrText());
        holder.editImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.textOcr.setVisibility(View.GONE);
                holder.etOcr.setVisibility(View.VISIBLE);
                holder.editImage.setVisibility(View.GONE);
                holder.saveImage.setVisibility(View.VISIBLE);
                holder.cancelImage.setVisibility(View.VISIBLE);
                holder.etOcr.setText(ocrList.getOcrText());
            }
        });
        holder.saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ocrList.setOcrText(holder.etOcr.getText().toString());
                InputMethodManager imm = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                notifyDataSetChanged();
            }
        });

        holder.cancelImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager imm = (InputMethodManager) v.getContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                notifyDataSetChanged();
            }
        });
       // holder.etOcr.setText(ocrList.getOcrText());
      /*  holder.etOcr.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               // if(ocrList.isSelected)
                  //   ocrList.setOcrText(s.toString().trim());
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ocrList.setOcrText(s.toString().trim());
             //   ocrListList.get(getAdapterPosition()).setEditTextValue(editText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
             //   ocrList.setOcrText(s.toString().trim());
            }
        });*/


    }

    @Override
    public int getItemCount() {
        return ocrListList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private RadioButton rbtSelected;
        private EditText etOcr;
        private TextView textOcr;
        private ImageView editImage;
        private ImageView saveImage;
        private ImageView cancelImage;
      //  public MyCustomEditTextListener myCustomEditTextListener;
        public MyViewHolder(@NonNull View itemView/*, MyCustomEditTextListener myCustomEditTextListener*/) {
            super(itemView);
            this.rbtSelected = itemView.findViewById(R.id.rb_Select);
            this.etOcr = itemView.findViewById(R.id.et_ocr_text);
            this.textOcr = itemView.findViewById(R.id.ocr_textview);
            this.editImage = itemView.findViewById(R.id.img_edit);
            this.saveImage = itemView.findViewById(R.id.img_save);
            this.cancelImage = itemView.findViewById(R.id.img_cancel);
          /*  this.myCustomEditTextListener = myCustomEditTextListener;
            this.etOcr.addTextChangedListener(myCustomEditTextListener);*/
        }
    }

    public static class OcrList {
        boolean isSelected = false;
        String ocrText;

        public OcrList() {
        }

        public OcrList(boolean isSelected, String ocrText) {
            this.isSelected = isSelected;
            this.ocrText = ocrText;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public String getOcrText() {
            return ocrText;
        }

        public void setOcrText(String ocrText) {
            this.ocrText = ocrText;
        }
    }

    View.OnClickListener first_radio_listener = new View.OnClickListener() {
        public void onClick(View v) {
            //Your Implementaions...
        }
    };

    private class MyCustomEditTextListener implements TextWatcher {
        private int position;

        public void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
           // ocrListList.get(position).setOcrText(charSequence.toString());
            // no op
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
         //   mDataset[position] = charSequence.toString();
            ocrListList.get(position).setOcrText(charSequence.toString());
        }

        @Override
        public void afterTextChanged(Editable editable) {
            //ocrListList.get(position).setOcrText(editable.toString());
            // no op
        }
    }
}
