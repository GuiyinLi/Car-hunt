'''
@Author: Ken Kaneki
@Date: 2020-09-10 21:05:36
@LastEditTime: 2020-09-10 22:49:00
@Description: README
@FilePath: \undefinedc:\Users\Dell\Desktop\优研面试\源码\服务器\IOT-evlocation\app.py
'''
from flask import *
import datetime
from DataBase_MySql import DB_MySql

# http://www.prover.work:443?execute=00  {"info": "OK"}
# http://www.prover.work:443?execute=10&longitude=116.853543&latitude=38.354140&direction=59.3
app = Flask(__name__)
rule = ['00', '10']  # 通信规则[第1位: 0-硬件端 1-APP端;第2位: 0-GET 1-POST]


def request_parse(req_data):
    if req_data.method == 'POST':
        data = req_data.json  # json数据
    elif req_data.method == 'GET':
        data = req_data.args  # 路径数据
    return data


@app.route('/', methods=['GET', 'POST'])
def get_data():
    db = DB_MySql()
    db.connectDatabase(host='39.97.235.93', user='root', password='170707109',
                       db='evdatabase')
    echo = {'status': 'null'}
    try:
        data = request_parse(request)
        print(data)
        execute = data.get('execute')
        if not execute in rule:
            echo = {'error': 'parameters error'}
            db.close()
            return jsonify(echo)
        elif execute == '00':  # 从服务器取数据[server->app]
            try:
                db.executeSql('select * from pos order by time DESC limit 1')
                queryResult = db.getCursorInfo()[1]
                time = queryResult[0].strftime('%Y-%m-%d %H:%M:%S')//获取查询结果集
                longitude = str(queryResult[1])//转数字
                latitude = str(queryResult[2])
                direction = str(queryResult[3])
            except Exception:#数据库提取数据异常时,默认位置为河北科技大学
                time=datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                longitude="114.528652"
                latitude="37.984026"
                direction="0.0"
            echo = {//创建存储字典
                "time": time,
                "longitude": longitude,
                "latitude": latitude,
                "direction": direction
            }
        elif execute == '10':  # 向服务器中存数据[hard->server]
            try:
                longitude = float(data.get('longitude'))
                latitude = float(data.get('latitude'))
                direction = float(data.get('direction'))
                now_time = datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')
                db.insert('pos', [now_time, longitude, latitude, direction])
                echo = {'info': 'OK'}
            except Exception:
                echo = {'error': 'message error'}
    except Exception:
        echo = {'error': 'illegal request'}
    db.close()
    return jsonify(echo)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=443, debug='True')

    # 虚拟环境的配置
    # /usr/local/python3/bin/pip3 install virtualenv
    # /usr/local/python3/bin/virtualenv  -p /usr/local/python3/bin/python3 venv
    # source venv/bin/activate
    # 激活成功后会出现(venv)
    # 退出虚拟环境命令是：deactivate
    # /usr/local/python3/bin/pip3 install  -r /home/project/YuBlog-master/source/requirements.txt
