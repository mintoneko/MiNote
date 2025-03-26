package net.micode.notes.ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser.NoteItemBgResources;

/**
 * 自定义列表项控件，负责：
 * 1. 显示笔记/文件夹/通话记录的列表项
 * 2. 根据数据类型动态配置显示元素（标题、时间、提醒图标、通话人信息）
 * 3. 管理不同笔记类型的背景样式
 * 4. 支持多选模式下的复选框状态
 * 5. 格式化时间显示（相对时间）
 * 6. 处理不同类型数据的显示逻辑（普通笔记、文件夹、通话记录）
 */
public class NotesListItem extends LinearLayout {
  private ImageView mAlert;
  private TextView mTitle;
  private TextView mTime;
  private TextView mCallName;
  private NoteItemData mItemData;
  private CheckBox mCheckBox;

  public NotesListItem(Context context) {
    super(context);
    inflate(context, R.layout.note_item, this);
    mAlert = (ImageView) findViewById(R.id.iv_alert_icon);
    mTitle = (TextView) findViewById(R.id.tv_title);
    mTime = (TextView) findViewById(R.id.tv_time);
    mCallName = (TextView) findViewById(R.id.tv_name);
    mCheckBox = (CheckBox) findViewById(android.R.id.checkbox);
  }

  public void bind(Context context, NoteItemData data, boolean choiceMode, boolean checked) {
    if (choiceMode && data.getType() == Notes.TYPE_NOTE) {
      mCheckBox.setVisibility(View.VISIBLE);
      mCheckBox.setChecked(checked);
    } else {
      mCheckBox.setVisibility(View.GONE);
    }

    mItemData = data;
    if (data.getId() == Notes.ID_CALL_RECORD_FOLDER) {
      mCallName.setVisibility(View.GONE);
      mAlert.setVisibility(View.VISIBLE);
      mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);
      mTitle.setText(context.getString(R.string.call_record_folder_name)
        + context.getString(R.string.format_folder_files_count, data.getNotesCount()));
      mAlert.setImageResource(R.drawable.call_record);
    } else if (data.getParentId() == Notes.ID_CALL_RECORD_FOLDER) {
      mCallName.setVisibility(View.VISIBLE);
      mCallName.setText(data.getCallName());
      mTitle.setTextAppearance(context, R.style.TextAppearanceSecondaryItem);
      mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet()));
      if (data.hasAlert()) {
        mAlert.setImageResource(R.drawable.clock);
        mAlert.setVisibility(View.VISIBLE);
      } else {
        mAlert.setVisibility(View.GONE);
      }
    } else {
      mCallName.setVisibility(View.GONE);
      mTitle.setTextAppearance(context, R.style.TextAppearancePrimaryItem);

      if (data.getType() == Notes.TYPE_FOLDER) {
        mTitle.setText(data.getSnippet()
          + context.getString(R.string.format_folder_files_count,
          data.getNotesCount()));
        mAlert.setVisibility(View.GONE);
      } else {
        mTitle.setText(DataUtils.getFormattedSnippet(data.getSnippet()));
        if (data.hasAlert()) {
          mAlert.setImageResource(R.drawable.clock);
          mAlert.setVisibility(View.VISIBLE);
        } else {
          mAlert.setVisibility(View.GONE);
        }
      }
    }
    mTime.setText(DateUtils.getRelativeTimeSpanString(data.getModifiedDate()));

    setBackground(data);
  }

  private void setBackground(NoteItemData data) {
    int id = data.getBgColorId();
    if (data.getType() == Notes.TYPE_NOTE) {
      if (data.isSingle() || data.isOneFollowingFolder()) {
        setBackgroundResource(NoteItemBgResources.getNoteBgSingleRes(id));
      } else if (data.isLast()) {
        setBackgroundResource(NoteItemBgResources.getNoteBgLastRes(id));
      } else if (data.isFirst() || data.isMultiFollowingFolder()) {
        setBackgroundResource(NoteItemBgResources.getNoteBgFirstRes(id));
      } else {
        setBackgroundResource(NoteItemBgResources.getNoteBgNormalRes(id));
      }
    } else {
      setBackgroundResource(NoteItemBgResources.getFolderBgRes());
    }
  }

  public NoteItemData getItemData() {
    return mItemData;
  }
}
