package com.paozhuanyinyu.eventtrack.android.plugin

import org.objectweb.asm.Opcodes

class EventTrackLambdaConfig{
    public final static HashMap<String, EventTrackMethodInfo> LAMBDA_METHODS = new HashMap<>();
    static{
        EventTrackMethodInfo onClick = new EventTrackMethodInfo(
                'onClick',
                '(Landroid/view/View;)V',
                'Landroid/view/View$OnClickListener;',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1,1,[Opcodes.ALOAD]
        )
        LAMBDA_METHODS.put(onClick.parent + onClick.name + onClick.desc, onClick)
        EventTrackMethodInfo onCheckdChanged = new EventTrackMethodInfo(
                'onCheckedChanged',
                '(Landroid/widget/CompoundButton;Z)V',
                'Landroid/widget/CompoundButton$OnCheckedChangeListener;',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1,1,[Opcodes.ALOAD]
        )
        LAMBDA_METHODS.put(onCheckdChanged.parent + onCheckdChanged.name + onCheckdChanged.desc, onCheckdChanged)
        EventTrackMethodInfo onRatingChanged = new EventTrackMethodInfo(
                'onRatingChanged',
                '(Landroid/widget/RatingBar;FZ)V',
                'Landroid/widget/RatingBar$OnRatingBarChangeListener;',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1,1,[Opcodes.ALOAD]
        )
        LAMBDA_METHODS.put(onRatingChanged.parent + onRatingChanged.name + onRatingChanged.desc, onRatingChanged)
        EventTrackMethodInfo onStopTrackingTouch = new EventTrackMethodInfo(
                'onStopTrackingTouch',
                '(Landroid/widget/SeekBar;)V',
                'Landroid/widget/SeekBar$OnSeekBarChangeListener;',
                'trackViewOnClick',
                '(Landroid/view/View;)V',
                1,1,[Opcodes.ALOAD]
        )
        LAMBDA_METHODS.put(onStopTrackingTouch.parent + onStopTrackingTouch.name + onStopTrackingTouch.desc, onStopTrackingTouch)
        EventTrackMethodInfo onDialogClick = new EventTrackMethodInfo(
                'onClick',
                '(Landroid/content/DialogInterface;I)V',
                'Landroid/content/DialogInterface$OnClickListener;',
                'trackViewOnClick',
                '(Landroid/content/DialogInterface;I)V',
                1,2,[Opcodes.ALOAD,Opcodes.ILOAD]
        )
        LAMBDA_METHODS.put(onDialogClick.parent + onDialogClick.name + onDialogClick.desc, onDialogClick)

        EventTrackMethodInfo onItemClick = new EventTrackMethodInfo(
                'onItemClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;IJ)V',
                'Landroid/widget/AdapterView$OnItemClickListener;',
                'trackViewOnClick',
                '(Landroid/widget/AdapterView;Landroid/view/View;I)V',
                1,3,[Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD]
        )
        LAMBDA_METHODS.put(onItemClick.parent + onItemClick.name + onItemClick.desc, onItemClick)

        EventTrackMethodInfo onGroupClick = new EventTrackMethodInfo(
                'onGroupClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IJ)Z',
                'Landroid/widget/ExpandableListView$OnGroupClickListener;',
                'trackExpandableListViewGroupOnClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;I)V',
                1,3,[Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD]
        )
        LAMBDA_METHODS.put(onGroupClick.parent + onGroupClick.name + onGroupClick.desc, onGroupClick)

        EventTrackMethodInfo onChildClick = new EventTrackMethodInfo(
                'onChildClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;IIJ)Z',
                'Landroid/widget/ExpandableListView$OnChildClickListener;',
                'trackExpandableListViewChildOnClick',
                '(Landroid/widget/ExpandableListView;Landroid/view/View;II)V',
                1,4,[Opcodes.ALOAD,Opcodes.ALOAD,Opcodes.ILOAD,Opcodes.ILOAD]
        )
        LAMBDA_METHODS.put(onChildClick.parent + onChildClick.name + onChildClick.desc, onChildClick)

        EventTrackMethodInfo onTabChanged = new EventTrackMethodInfo(
                'onTabChanged',
                '(Ljava/lang/String;)V',
                'Landroid/widget/TabHost$OnTabChangeListener;',
                'trackTabHost',
                '(Ljava/lang/String;)V',
                1,1,[Opcodes.ALOAD]
        )
        LAMBDA_METHODS.put(onTabChanged.parent + onTabChanged.name + onTabChanged.desc, onTabChanged)
    }
}