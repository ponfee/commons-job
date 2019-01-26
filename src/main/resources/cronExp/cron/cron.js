/**
 * 每周期
 */
function everyTime(dom) {
    var item = $("input[name=v_" + dom.name + "]");
    item.val("*");
    item.change();
}

/**
 * 不指定
 */
function unAppoint(dom) {
    var name = dom.name;
    var val = "?";
    if (name == "year")
        val = "";
    var item = $("input[name=v_" + name + "]");
    item.val(val);
    item.change();
}

/**
 * 周期
 */
function cycle(dom) {
    var name = dom.name;
    var ns = $(dom).parent().find(".numberspinner");
    var start = ns.eq(0).numberspinner("getValue");
    var end = ns.eq(1).numberspinner("getValue");
    var item = $("input[name=v_" + name + "]");
    item.val(start + "-" + end);
    item.change();
}

/**
 * 从开始
 */
function startOn(dom) {
    var name = dom.name;
    var ns = $(dom).parent().find(".numberspinner");
    var start = ns.eq(0).numberspinner("getValue");
    var end = ns.eq(1).numberspinner("getValue");
    var item = $("input[name=v_" + name + "]");
    item.val(start + "/" + end);
    item.change();
}

function lastDay(dom){
    var item = $("input[name=v_" + dom.name + "]");
    item.val("L");
    item.change();
}

function weekOfDay(dom){
    var name = dom.name;
    var ns = $(dom).parent().find(".numberspinner");
    var start = ns.eq(0).numberspinner("getValue");
    var end = ns.eq(1).numberspinner("getValue");
    var item = $("input[name=v_" + name + "]");
    item.val(start + "#" + end);
    item.change();
}

function lastWeek(dom){
    var item = $("input[name=v_" + dom.name + "]");
    var ns = $(dom).parent().find(".numberspinner");
    var start = ns.eq(0).numberspinner("getValue");
    item.val(start+"L");
    item.change();
}

function workDay(dom) {
    var name = dom.name;
    var ns = $(dom).parent().find(".numberspinner");
    var start = ns.eq(0).numberspinner("getValue");
    var item = $("input[name=v_" + name + "]");
    item.val(start + "W");
    item.change();
}

$(function() {
    $(".numberspinner").numberspinner({
        onChange:function(){
            $(this).closest("div.line").children().eq(0).click();
        }
    });

    var vals = $("input[name^='v_']");
    var cronExp = $(":text[name='cronExp']");
    vals.change(function() {
        var item = [];
        vals.each(function() {
            item.push(this.value);
        });
        cronExp.val(item.join(" "));
    });
    
    // 秒
    var secondList = $(".secondList").children();
    $("#sencond_appoint").click(function(){
        if(this.checked){
            secondList.eq(0).change();
        }
    });
    secondList.change(function() {
        $("#"+$(this).parent().attr("for")).prop("checked", true);
        var sencond_appoint = $("#sencond_appoint").prop("checked");
        if (sencond_appoint) {
            var vals = [];
            secondList.each(function() {
                if (this.checked) {
                    vals.push(this.value);
                }
            });
            var val = "?";
            if (vals.length > 0 && vals.length < 59) {
                val = vals.join(","); 
            }else if(vals.length == 59){
                val = "*";
            }
            var item = $("input[name=v_second]");
            item.val(val);
            item.change();
        }
    });

    // 分钟
    var minList = $(".minList").children();
    $("#min_appoint").click(function(){
        if(this.checked){
            minList.eq(0).change();
        }
    });
    minList.change(function() {
        $("#"+$(this).parent().attr("for")).prop("checked", true);
        var min_appoint = $("#min_appoint").prop("checked");
        if (min_appoint) {
            var vals = [];
            minList.each(function() {
                if (this.checked) {
                    vals.push(this.value);
                }
            });
            var val = "?";
            if (vals.length > 0 && vals.length < 59) {
                val = vals.join(",");
            }else if(vals.length == 59){
                val = "*";
            }
            var item = $("input[name=v_min]");
            item.val(val);
            item.change();
        }
    });
    
    // 小时
    var hourList = $(".hourList").children();
    $("#hour_appoint").click(function(){
        if(this.checked){
            hourList.eq(0).change();
        }
    });
    hourList.change(function() {
        $("#"+$(this).parent().attr("for")).prop("checked", true);
        var hour_appoint = $("#hour_appoint").prop("checked");
        if (hour_appoint) {
            var vals = [];
            hourList.each(function() {
                if (this.checked) {
                    vals.push(this.value);
                }
            });
            var val = "?";
            if (vals.length > 0 && vals.length < 24) {
                val = vals.join(",");
            }else if(vals.length == 24){
                val = "*";
            }
            var item = $("input[name=v_hour]");
            item.val(val);
            item.change();
        }
    });
    
    // 日期
    var dayList = $(".dayList").children();
    $("#day_appoint").click(function(){
        if(this.checked){
            dayList.eq(0).change();
        }
    });
    dayList.change(function() {
        $("#"+$(this).parent().attr("for")).prop("checked", true);
        var day_appoint = $("#day_appoint").prop("checked");
        if (day_appoint) {
            var vals = [];
            dayList.each(function() {
                if (this.checked) {
                    vals.push(this.value);
                }
            });
            var val = "?";
            if (vals.length > 0 && vals.length < 31) {
                val = vals.join(",");
            }else if(vals.length == 31){
                val = "*";
            }
            var item = $("input[name=v_day]");
            item.val(val);
            item.change();
        }
    });
    
    // 月
    var mouthList = $(".mouthList").children();
    $("#mouth_appoint").click(function(){
        if(this.checked){
            mouthList.eq(0).change();
        }
    });
    mouthList.change(function() {
        $("#"+$(this).parent().attr("for")).prop("checked", true);
        var mouth_appoint = $("#mouth_appoint").prop("checked");
        if (mouth_appoint) {
            var vals = [];
            mouthList.each(function() {
                if (this.checked) {
                    vals.push(this.value);
                }
            });
            var val = "?";
            if (vals.length > 0 && vals.length < 12) {
                val = vals.join(",");
            }else if(vals.length == 12){
                val = "*";
            }
            var item = $("input[name=v_mouth]");
            item.val(val);
            item.change();
        }
    });
    
    // 周
    var weekList = $(".weekList").children();
    $("#week_appoint").click(function(){
        if(this.checked){
            weekList.eq(0).change();
        }
    });
    weekList.change(function() {
        $("#"+$(this).parent().attr("for")).prop("checked", true);
        var week_appoint = $("#week_appoint").prop("checked");
        if (week_appoint) {
            var vals = [];
            weekList.each(function() {
                if (this.checked) {
                    vals.push(this.value);
                }
            });
            var val = "?";
            if (vals.length > 0 && vals.length < 7) {
                val = vals.join(",");
            }else if(vals.length == 7){
                val = "*";
            }
            var item = $("input[name=v_week]");
            item.val(val);
            item.change();
        }
    });
    
    $("ul.tabs > li").on("click", function (index, item){
        reverseParse(); // 反解析
    });
    reverseParse(); // 反解析
});
