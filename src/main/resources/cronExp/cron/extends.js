/**
 * ------------------------
 * 对ECAM Script一些原生类的原型链进行扩展
 * @author Ponfee
 * ------------------------
 */
String.prototype.startWith = function(str) {
    return new RegExp("^" + str).test(this);
};
String.prototype.endWith = function(str) {
    return new RegExp(str + "$").test(this);
};
String.prototype.isNull = function() {
    return this == undefined || this == null;
};
String.prototype.isEmpty = function() {
    return /^\s*$/.test(this);
};
String.prototype.format = function(args) {
    if (arguments.length < 1) return this;
    var result = this;
    if (arguments.length == 1 && typeof args === "object" 
        && Object.prototype.toString.apply(args) !== "[object Array]") {
        for (var key in args) {
            var reg = new RegExp("({:" + key + "})", "g");
            result = result.replace(reg, args[key]);
        }
    } else {
        if (arguments.length > 1) args = Array.prototype.slice.call(arguments); // arguments并非真正的数组，所以要转换
        else if (arguments.length == 1 && Object.prototype.toString.apply(args) !== "[object Array]") args = [args];
        for (var i = 0; i < args.length; i++) {
            var reg= new RegExp("({:" + i + "})", "g");
            result = result.replace(reg, args[i]);
        }
    }
    return result;
};
String.prototype.trim=function() {
    return this.replace(/^[\r\n\s\t]*|[\r\n\s\t]*$/g, "");
};
String.prototype.replaceAll = function(reallyDo, replaceWith, ignoreCase) {
    if (!RegExp.prototype.isPrototypeOf(reallyDo)) {
        return this.replace(new RegExp(reallyDo, (ignoreCase ? "gi" : "g")), replaceWith);
    } else {
        return this.replace(reallyDo, replaceWith);
    }
};
String.prototype.toHex=function (){
    var val = "";
    for ( var i = 0; i < this.length; i++) {
        val += this.charCodeAt(i).toString(16);
    }
    return val;
}
/**
 * ------------------------
 * 对Date的扩展，将 Date 转化为指定格式的String
 * 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，
 * 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)
 * 例子：
 * (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423
 * (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18
 * ------------------------
 */
Date.prototype.format=function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "h+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) {
      fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    }
    for (var k in o) {
        if (new RegExp("(" + k + ")").test(fmt)) {
          fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
        }
    }
    return fmt;
};

/** contains() method for Array */
Array.prototype.contains = function(item) {
    return RegExp("(^|,)" + item.toString() + "($|,)").test(this);
};
Array.prototype.remove = function(item) {
    var index = this.indexOf(item);
    if (index > -1) {
        this.splice(index, 1);
    }
};

/**
 * window.open()封装
 * @param {Object} url
 * @param {Object} target
 * @param {Object} winname
 * @param {Object} params
 */
function openWin(url, method, params, target, winname) {
    var tempForm = document.createElement("form");
    var time = new Date().getTime();
    tempForm.id = "form_"+time;
    tempForm.action = url;
    tempForm.method = method || 'post';
    tempForm.target = target || '_blank';
    winname = winname || 'win_'+time;
    params = params || {};
    var hideInput = null;
    for ( var name in params) {
        hideInput = document.createElement("input");
        hideInput.type = "hidden";
        hideInput.name = name;
        hideInput.value = params[name];
        tempForm.appendChild(hideInput);
    }
    document.body.appendChild(tempForm);
    tempForm.submit();
    document.body.removeChild(tempForm);
}

/**
 * 打开模态窗口
 * @param {Object} url
 * @param {Object} params
 * @param {Object} whparam
 * @param {Object} e
 * @return {TypeName} 
 */
function openModalDialog(url, params, e, whparam) {
    var sFeatures = "help:no;status:no;center:yes;";
    if (whparam) {
        // 相对于浏览器的居中位置   
        var bleft = (screen.width - whparam.width) / 2;
        var btop = (screen.height - whparam.height) / 2;
    
        // 根据鼠标点击位置算出绝对位置   
        var tleft = e.screenX - e.clientX;
        var ttop = e.screenY - e.clientY;
    
        // 最终模态窗口的位置   
        var left = bleft + tleft;
        var top = btop + ttop;
        sFeatures += "dialogLeft:" + left + "px;dialogTop:" + top + "px;";
    } else {
        whparam = {width: screen.width-10, height: screen.height-70};
    }
    sFeatures += "dialogWidth:" + whparam.width + "px;dialogHeight:" + whparam.height + "px;";

    return window.showModalDialog(url, params || {}, sFeatures);
}

/**
 * 阻止事件冒泡
 * @param {Object} evt
 */
function preventEvent(evt) {
    evt = evt ? evt : (window.event ? window.event : null);
    if (window.event) {
        evt.returnValue = false;
        evt.cancelBubble = true;
    } else {
        evt.preventDefault();
        evt.stopPropagation();
    }
    return evt;
}

/**
 * 说明：本代码可自由复制修改并且使用，但请保留作者信息！
 * JS 操作 URL 函数使用说明：
 * 初始化 var myurl=new UrlHandler(); // 也可以自定义URL： var myurl=new UrlHandler('http://iulog.com/?sort=js'); 
 * 读取url参数值 var val=myurl.get('abc'); // 读取参数abc的值
 * 设置url参数 myurl.set("arg",data); // 新增/修改 一个arg参数的值为data
 * 移除url参数 myurl.remove("arg"); // 移除arg参数
 * 获取处理后的URL myurl.url(); // 一般就直接执行转跳 location.href=myurl.url();
 * 调试接口：myurl.debug(); // 修改该函数进行调试
 * @param {Object} url
 * @memberOf {TypeName} 
 * @return {TypeName} 
 */
function UrlHandler(url) {
    var ourl=url||window.location.href;
    var _href="";//?前面部分
    var params={};//url参数对象
    var _anchor="";//#及后面部分
    var init=function(){
        var str=ourl;
        var index=str.indexOf("#");
        if(index>0){
            _anchor=str.substr(index);
            str=str.substring(0,index);
        }
        index=str.indexOf("?");
        if(index>0){
            _href=str.substring(0,index);
            str=str.substr(index+1);
            var parts=str.split("&");
            for(var i=0;i<parts.length;i++){
                var kv=parts[i].split("=");
                params[kv[0]]=kv[1];
            }
        }else{
            _href=ourl;
            params={};
        }
    };
    this.set=function(key,val){
        params[key]=encodeURIComponent(val);
    };
    this.remove=function(key){
        if(key in params) params[key]=undefined;
    };
    this.get=function(key){
        return params[key];
    };
    this.url=function(key){
        var strurl=_href;
        var objps=[];
        for(var k in params){
            if(params[k]){
                objps.push(k+"="+params[k]);
            }
        }
        if(objps.length>0){
            strurl+="?"+objps.join("&");
        }
        if(_anchor.length>0){
            strurl+=_anchor;
        }
        return strurl;
    };
    this.debug = function() { // 以下调试代码自由设置
        var objps=[];
        for(var k in params){
            objps.push(k+"="+params[k]);
        }
        console.log(objps);
    };
    init();
}

/**
 * 校验上传文件(文件大小只适用图片格式)
 * @param {Object} target
 * @param {Object} id
 * @return {TypeName} 
 */
function verifyFile(target, fileInput, allows, maxSize) {
    fileInput = $("input[type='text'][name='"+fileInput+"']");
    extension =target.value.substr(target.value.lastIndexOf(".")).toLowerCase(); //获得文件后缀名
    if(allows && !allows.split(",").contains(extension)){
        alert("请上传["+allows+"]格式的文件！");
        target.outerHTML = target.outerHTML;
        fileInput.val('');
        fileInput.focus();
        return false;
    }

    var fileName = target.value;
    var fileSize = 0;
    if (!target.files && (/msie/i.test(navigator.userAgent) && !window.opera)) {
        try {
            target.select();  
            if (top != self) window.parent.document.body.focus();
            else  target.blur();
            var filePath = document.selection.createRange().text;
            document.selection.empty();
            var fso = new ActiveXObject("Scripting.FileSystemObject");
            if(!fso.FileExists(filePath)){ 
                alert("附件不存在，请重新输入！");
                fileInput.val('');
                return false; 
            } 
            fileSize = fso.GetFile(filePath).size;
        } catch (e) {
            // alert("请更改IE设置： IE -> Internet选项 -> 安全 -> 自定义级别 -> ActiveX控件和插件 -> 对未标记为可安全执行脚本的ActiveX控件初始化并执行脚本（不安全） -> 启用")
            fileSize = 1;
        }
    } else {
        fileSize = target.files[0].size;
    }
    
    if (fileSize > (maxSize || 1024*1024*2)) {
        alert("文件大小超过限制");
        target.outerHTML = target.outerHTML;
        fileInput.focus();
        $(target).focus();
        fileName = '';
    }
    if (fileSize <= 0) {
        alert("文件不能为空！");
        target.outerHTML = target.outerHTML;
        fileInput.focus();
        $(target).focus();
        fileName = '';
    }

    fileInput.val(fileName);
}
