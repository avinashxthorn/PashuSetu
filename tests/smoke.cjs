const { chromium }=require('playwright');
const http=require('node:http'),fs=require('node:fs'),assert=require('node:assert/strict');
(async()=>{
const server=http.createServer((req,res)=>{res.writeHead(200,{'Content-Type':'text/html; charset=utf-8'});res.end(fs.readFileSync('index.html'))});
await new Promise(r=>server.listen(0,'127.0.0.1',r));
let browser;
try{
browser=await chromium.launch({headless:true});
const page=await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true});
const errors=[];page.on('pageerror',e=>errors.push(e.message));
await page.goto('http://127.0.0.1:'+server.address().port);
await page.getByRole('heading',{name:'Your herd, in good hands.'}).waitFor();
assert.equal(await page.locator('main').getAttribute('aria-busy'),'false');
await page.getByRole('button',{name:'Register animal'}).click();
await page.locator('[name=name]').fill('Gauri Test');
await page.locator('[name=tag]').fill('TEST-001');
await page.locator('[name=species]').selectOption('Cow');
await page.locator('[name=status]').selectOption('Healthy');
await page.locator('#entry button[type=submit]').click();
await page.getByRole('heading',{name:'Gauri Test'}).waitFor();
await page.reload();
await page.getByRole('heading',{name:'Your herd, in good hands.'}).waitFor();
assert.equal(await page.locator('.stat b').first().textContent(),'1');
await page.locator('nav [data-action="go:animals"]').click();
await page.locator('#search').fill('TEST-001');
await page.locator('.animal').click();
await page.locator('[data-action="record:vaccine"]').click();
await page.locator('[name=title]').fill('Test vaccination');
await page.locator('[name=due]').fill('2099-01-01');
await page.locator('#entry button[type=submit]').click();
await page.getByText('Test vaccination',{exact:false}).first().waitFor();
await page.locator('nav [data-action="go:reports"]').click();
await page.getByRole('heading',{name:'Farm reports'}).waitFor();
assert.equal(await page.locator('.donut').count(),3);
assert.equal(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true,'Mobile page must not overflow');
assert.deepEqual(errors,[]);
console.log('PASS: load, register, persist, search, vaccine, reports, mobile layout and runtime errors');
}finally{if(browser)await browser.close();server.close()}
})().catch(e=>{console.error(e);process.exitCode=1});
