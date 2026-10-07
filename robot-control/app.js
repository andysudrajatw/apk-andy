const state={connected:false,mode:'MANUAL',speed:60,last:'STOP',events:[]};
const $=s=>document.querySelector(s); const $$=s=>document.querySelectorAll(s);
const defaultSketch=`// Mobil Robot Arduino - 4 roda
// Protokol sederhana: FORWARD, BACKWARD, LEFT, RIGHT, STOP

const int ENA=5, IN1=7, IN2=8;
const int ENB=6, IN3=9, IN4=10;

void setup(){
  Serial.begin(115200);
  pinMode(ENA,OUTPUT); pinMode(IN1,OUTPUT); pinMode(IN2,OUTPUT);
  pinMode(ENB,OUTPUT); pinMode(IN3,OUTPUT); pinMode(IN4,OUTPUT);
  stopRobot();
}

void loop(){
  if(Serial.available()){
    String cmd=Serial.readStringUntil('\\n');
    cmd.trim();
    if(cmd=="FORWARD") forward(180);
    else if(cmd=="BACKWARD") backward(180);
    else if(cmd=="LEFT") left(160);
    else if(cmd=="RIGHT") right(160);
    else stopRobot();
  }
}

void forward(int pwm){ motorA(pwm,true); motorB(pwm,true); }
void backward(int pwm){ motorA(pwm,false); motorB(pwm,false); }
void left(int pwm){ motorA(0,true); motorB(pwm,true); }
void right(int pwm){ motorA(pwm,true); motorB(0,true); }
void stopRobot(){ motorA(0,true); motorB(0,true); }

void motorA(int pwm,bool forwardDir){
  digitalWrite(IN1,forwardDir?HIGH:LOW); digitalWrite(IN2,forwardDir?LOW:HIGH);
  analogWrite(ENA,pwm);
}
void motorB(int pwm,bool forwardDir){
  digitalWrite(IN3,forwardDir?HIGH:LOW); digitalWrite(IN4,forwardDir?LOW:HIGH);
  analogWrite(ENB,pwm);
}`;
function log(msg){const t=new Date().toLocaleTimeString('id-ID');state.events.unshift(t+'  '+msg);state.events=state.events.slice(0,100); renderLog();}
function renderLog(){const html=state.events.map(x=>'<div class="log-item">'+x+'</div>').join('')||'<div class="muted small">Belum ada event.</div>'; $('#commandLog').innerHTML=html; $('#telemetryLog').innerHTML=html; $('#logCount').textContent=state.events.length+' event';}
function setConnection(on){state.connected=on; $('#statusText').textContent=on?'Terhubung':'Demo / Offline'; $('#statusDot').style.background=on?'var(--accent)':'var(--danger)'; $('#connectionPill').textContent=on?'ONLINE':'OFFLINE'; $('#connectBtn').textContent=on?'Putuskan':'Hubungkan Robot'; if(on)log('Koneksi robot aktif');else log('Koneksi diputus');}
function command(cmd){state.last=cmd;$('#lastCommand').textContent=cmd;$('#directionBadge').textContent=cmd; $('#modeValue').textContent=state.mode; const pwm=Math.round(state.speed*2.55); $('#leftMotor').textContent=(cmd==='LEFT'?0:cmd==='RIGHT'?state.speed:cmd==='STOP'?0:state.speed)+'%'; $('#rightMotor').textContent=(cmd==='RIGHT'?0:cmd==='LEFT'?state.speed:cmd==='STOP'?0:state.speed)+'%'; log('CMD '+cmd+' • PWM '+pwm); sendToRobot({command:cmd,speed:state.speed});}
async function sendToRobot(payload){
  if(!state.connected)return;
  const host=$('#hostInput').value.trim();
  const port=$('#portInput').value;
  const protocol=$('#protocolInput').value;
  // Same-origin mode: when this dashboard is served by the ESP32/local gateway,
  // use relative /api endpoints and avoid browser mixed-content restrictions.
  const sameOrigin = location.protocol.startsWith('http') && (location.hostname===host || !host);
  try{
    if(protocol==='HTTP'){
      const url = sameOrigin ? '/api/command' : 'http://'+host+':'+port+'/api/command';
      await fetch(url,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(payload),mode:'cors'});
    }else{
      const scheme = location.protocol==='https:' ? 'wss://' : 'ws://';
      const url = sameOrigin ? scheme+location.host : 'ws://'+host+':'+port;
      window.robotSocket=window.robotSocket||new WebSocket(url);
      if(window.robotSocket.readyState===1)window.robotSocket.send(JSON.stringify(payload));
    }
  }catch(e){log('Gagal kirim: '+e.message);}
}
function go(tab){$$('.nav-item').forEach(x=>x.classList.toggle('active',x.dataset.tab===tab));$$('.tab').forEach(x=>x.classList.toggle('active',x.id==='tab-'+tab));const titles={dashboard:'Dashboard Robot',control:'Kontrol Robot',program:'Program Arduino',telemetry:'Monitoring',settings:'Koneksi Perangkat'};$('#pageTitle').textContent=titles[tab]||'Robot Control Center';}
$$('.nav-item').forEach(b=>b.addEventListener('click',()=>go(b.dataset.tab))); $$('[data-goto]').forEach(b=>b.addEventListener('click',()=>go(b.dataset.goto)));
$('#connectBtn').addEventListener('click',()=>setConnection(!state.connected)); $('#emergencyStop').addEventListener('click',()=>command('STOP'));
$$('[data-cmd]').forEach(b=>b.addEventListener('click',()=>command(b.dataset.cmd)));
$('#speedRange').addEventListener('input',e=>{state.speed=+e.target.value;$('#speedOutput').textContent=state.speed+'%';$('#speedValue').textContent=state.speed+'%';});
$$('.mode').forEach(b=>b.addEventListener('click',()=>{$$('.mode').forEach(x=>x.classList.remove('active'));b.classList.add('active');state.mode=b.dataset.mode;$('#modeValue').textContent=state.mode;log('MODE '+state.mode);}));
$$('.toggle').forEach(b=>b.addEventListener('click',()=>{b.classList.toggle('on');b.setAttribute('aria-pressed',b.classList.contains('on'));log((b.id==='lightToggle'?'HEADLIGHT':'BUZZER')+' '+(b.classList.contains('on')?'ON':'OFF'));}));
$('#codeEditor').value=localStorage.getItem('robotSketch')||defaultSketch;
$('#saveSketch').addEventListener('click',()=>{localStorage.setItem('robotSketch',$('#codeEditor').value);$('#compileStatus').textContent='SAVED';log('Sketch disimpan di browser');});
$('#copySketch').addEventListener('click',async()=>{await navigator.clipboard.writeText($('#codeEditor').value);log('Kode disalin ke clipboard');});
$('#validateCode').addEventListener('click',()=>{const code=$('#codeEditor').value;const ok=code.includes('setup')&&code.includes('loop')&&code.includes('stopRobot');$('#compileStatus').textContent=ok?'VALID':'CHECK';log(ok?'Program valid secara struktur dasar':'Program perlu diperiksa');});
$('#clearLog').addEventListener('click',()=>{state.events=[];renderLog();});
$('#saveConnection').addEventListener('click',()=>{localStorage.setItem('robotHost',$('#hostInput').value);localStorage.setItem('robotPort',$('#portInput').value);localStorage.setItem('robotProtocol',$('#protocolInput').value);log('Konfigurasi koneksi disimpan');});
document.addEventListener('keydown',e=>{if(['INPUT','TEXTAREA','SELECT'].includes(document.activeElement.tagName))return;const map={ArrowUp:'FORWARD',w:'FORWARD',W:'FORWARD',ArrowDown:'BACKWARD',s:'BACKWARD',S:'BACKWARD',ArrowLeft:'LEFT',a:'LEFT',A:'LEFT',ArrowRight:'RIGHT',d:'RIGHT',D:'RIGHT',' ':'STOP'};if(map[e.key]){e.preventDefault();command(map[e.key]);}});
setInterval(()=>{const d=Math.max(20,Math.round(60+Math.random()*40));$('#distanceValue').textContent=d+' cm';$('#distance2').textContent=d+' cm';const bat=Math.max(10,Math.round(+($('#batteryValue').textContent.replace('%',''))-(Math.random()<.08?1:0)));$('#batteryValue').textContent=bat+'%';$('#voltageValue').textContent=(10.8+bat*0.012).toFixed(1)+' V';$('#tempValue').textContent=(28.5+Math.random()*2).toFixed(1)+'°C';},2200);
setInterval(()=>{$('#clock').textContent=new Date().toLocaleString('id-ID',{dateStyle:'medium',timeStyle:'medium'});},1000);
const savedHost=localStorage.getItem('robotHost');if(savedHost)$('#hostInput').value=savedHost;const savedPort=localStorage.getItem('robotPort');if(savedPort)$('#portInput').value=savedPort;const savedProtocol=localStorage.getItem('robotProtocol');if(savedProtocol)$('#protocolInput').value=savedProtocol;renderLog();