import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 10,
    duration: '5s',
    thresholds: {
        http_req_failed: ['rate<0.5'],
        http_req_duration: ['p(95)<2000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080/api';
const TOKEN = __ENV.TOKEN || '';
const ORDER_ID = __ENV.ORDER_ID || '';

export default function () {
    const res = http.post(
        `${BASE_URL}/orders/${ORDER_ID}/pay`,
        null,
        {
            headers: {
                Authorization: `Bearer ${TOKEN}`,
            },
        }
    );

    check(res, {
        'status is 200, 409, or 429': (r) =>
            r.status === 200 || r.status === 409 || r.status === 429,
    });

    sleep(1);

}
