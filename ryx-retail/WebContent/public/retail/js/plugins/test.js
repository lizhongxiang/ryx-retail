var userSelection;
if (window.getSelection) { //现代浏览器
    userSelection = window.getSelection();
} else if (document.selection) { //IE浏览器 考虑到Opera，应该放在后面
    userSelection = document.selection.createRange();
}


var sel = win.document.selection; // IE
var sel = win.getSelection(); // DOM

var range = sel.createRange(); // IE下
var range = sel.getRangeAt(0); // DOM下

if(range.startContainer){ // DOM下
sel.removeAllRanges(); // 删除Selection中的所有Range
range.deleteContents(); // 清除Range中的内容
// 获得Range中的第一个html结点
var container = range.startContainer; 
// 获得Range起点的位移
var pos = range.startOffset; 
// 建一个空Range
range = document.createRange(); 
// 插入内容
var cons = win.document.createTextNode(",:),"); 

if(container.nodeType == 3){// 如是一个TextNode
container.insertData(pos, cons.nodeValue); 
// 改变光标位置
range.setEnd(container, pos + cons.nodeValue.length); 
range.setStart(container, pos + cons.nodeValue.length); 
}else{// 如果是一个HTML Node
var afternode = container.childNodes[pos]; 
container.insertBefore(cons, afternode); 

range.setEnd(cons, cons.nodeValue.length); 
range.setStart(cons, cons.nodeValue.length); 
} 
sel.addRange(range); 
}else{// IE下
var cnode = range.parentElement(); 
while(cnode.tagName.toLowerCase() != “body”){ 
cnodecnode = cnode.parentNode; 
} 
if(cnode.id && cnode.id==”rich_txt_editor”){ 
range.pasteHTML(",:),"); 
} 
} 
win.focus(); 


function get_selection(the_id)
{
     var e = typeof(the_id)=='String'? document.getElementById(the_id) : the_id;

    //Mozilla and DOM 3.0
    if('selectionStart' in e)
    {
        var l = e.selectionEnd - e.selectionStart;
        return { start: e.selectionStart, end: e.selectionEnd, length: l, text: e.value.substr(e.selectionStart, l) };
    }
    //IE
    else if(document.selection)
    {
        e.focus();
        var r = document.selection.createRange();
        var tr = e.createTextRange();
        var tr2 = tr.duplicate();
        tr2.moveToBookmark(r.getBookmark());
        tr.setEndPoint('EndToStart',tr2);
        if (r == null || tr == null) return { start: e.value.length, end: e.value.length, length: 0, text: '' };
        var text_part = r.text.replace(/[\r\n]/g,'.'); //for some reason IE doesn't always count the \n and \r in the length
        var text_whole = e.value.replace(/[\r\n]/g,'.');
        var the_start = text_whole.indexOf(text_part,tr.text.length);
        return { start: the_start, end: the_start + text_part.length, length: text_part.length, text: r.text };
    }
    //Browser not supported
    else return { start: e.value.length, end: e.value.length, length: 0, text: '' };
