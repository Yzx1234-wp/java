CHROME="/c/Program Files (x86)/Google/Chrome/Application/chrome.exe"
PROF="C:/Users/Administrator/AppData/Local/Temp/cbprobe"
PY=C:/Users/Administrator/.workbuddy/binaries/python/versions/3.13.12/python.exe
run_case() {
  local name="$1"; local qs="$2"
  rm -rf "$PROF" 2>/dev/null
  "$CHROME" --headless=new --disable-gpu --no-sandbox --user-data-dir="$PROF" --virtual-time-budget=20000 --dump-dom "http://localhost:5173/__probe.html$qs" > "_d.html" 2>/dev/null
  $PY -c "
import re,sys
sys.stdout.reconfigure(encoding='utf-8')
h=open('_d.html',encoding='utf-8').read()
cards=re.findall(r'<article class=\"art-card\"[^>]*>(.*?)</article>', h, re.S)
names=[re.search(r'<h4>([^<]+)</h4>',c).group(1) for c in cards] if cards else []
m=re.search(r'<h3[^>]*>(.*?)</h3>', h, re.S)
head=re.sub(r'<[^>]+>','',m.group(1)).strip() if m else 'N/A'
empty='未检索到' in h
print('$name'.ljust(30), '命中=%-2d' % len(cards), '|', head, '| 空态' if empty else '')
if names: print(' '*30, ' ->', '、'.join(names[:6]) + (' …' if len(names)>6 else ''))
"
}
run_case "① 搜索『雷』"                 "?q=%E9%9B%B7"
run_case "② 搜索『至尊骨』"             "?q=%E8%87%B3%E5%B0%8A%E9%AA%A8"
run_case "③ 分类=宝术"                  "?cat=%E5%AE%9D%E6%9C%AF"
run_case "④ 分级=绝世"                  "?grade=%E7%BB%9D%E4%B8%96"
run_case "⑤ 分类=宝术+分级=绝顶"        "?cat=%E5%AE%9D%E6%9C%AF&grade=%E7%BB%9D%E9%A1%B6"
run_case "⑥ 排序=按名称(拼音序)"        "?sort=%E6%8C%89%E5%90%8D%E7%A7%B0"
run_case "⑦ 搜索无关词『斗破苍穹』"     "?q=%E6%96%97%E7%A0%B4%E8%8B%8D%E7%A9%B9"
run_case "⑧ 搜索+分类组合『火』+宝术"   "?q=%E7%81%AB&cat=%E5%AE%9D%E6%9C%AF"
