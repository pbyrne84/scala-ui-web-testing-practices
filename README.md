# UI Web Testing Practices (exampled in Scala and Selenium)

## Getting Chrome Driver

https://googlechromelabs.github.io/chrome-for-testing/
https://developer.chrome.com/docs/chromedriver/get-started

Rename download to 
```scala
chromedriver.zip
```

## Why do we test like this as a developer?

Clicking around UI's while developing manually trying to prove ourselves correct as we go becomes possibly the biggest 
time waste we can do. The more complicated the user journey, the bigger the time waste. This is not a 20% time waste but 
going into the realms of 10x time waste. This is not a hyperbolic statement, if you surmise how much time is spent 
clicking around, often clumsily with errors, versus time taken to actually code the solution, the waste becomes clear.
It is easily perceivable if you train yourself to perceive it, and if you have the efficiency 'gene', you are likely to notice it 
but don't know how to deal with it. In fact the efficiency 'gene' may be shouting at you to find something else to do that
is not a time waste.


## Why do developers do test automation?
Your first answer will possibly be "Prove what I just did was right". Thinking like that is a self-limiting short-sighted 
over simplification that leads to most environments ending up as cargo cults. We have a habit of liking to learn by direct
experience, but we seem to be incredibly poor at recognising when this is a massive time waste, not just for ourselves, but
waste the time of others around us. I don't like reading instructions, it is more fun working things out, but professionally
we should ask the deep questions, like "Why did this come about? What problems were people having".

Personally, I do test automation to make it easy for the next person to do the next change. Programming is usually a communial
activity, often with people we will never meet or talk to, so it is nice if we try not to leave people with headaches. Other
people are not there to clean up our shortcuts/sloppy decisions and often have enough problems without us adding to them.

### Test pyramids

This is where testing pyramids come in. This can be quite a large topic as how you can effectively test the system changes 
depending on the depth and complexity of the system. End to end only starts having a lot of trouble hitting boundaries 
without a lot of misdirecting complication becomes harder and harder to work around. The tests become a boat anchor when it
comes to change, when it should be the engine that allows it to move.

#### Questions to ask when dealing with test/code organisation

1. If I change a piece of code and no tests fail, how much effort would it be to add a test. I have seen projects where the 
   effort is way too high. Usually this is when there is too much end-to-end testing. You want to add a test for something like
   currency rounding, and now you are potentially messing with IO setup. Testing maths/pure logic is the nicest stuff to test.
   Why add a lot of misdirecting complications that also have to potentially be debugged on failure.
2. Are the tests designed around communicating failure clearly? We have different levels of seniority/ability in different 
   aspects. Writing tests where you have to change code to see the actual failure is fairly junior in attitude. Seniors think
   about how their actions affect others, juniors are focussing more on whether they can do it. We can have a senior in your
   job title, but ability wise be a junior in areas we have not actually exerted to gain growth in.  
   The failure could be in CI where we cannot change code and also can be intermittent, adding to the problems. 
   Seeing the failure clearly helps us potentially spot a pattern so we can fix it, etc. 
3. Can you easily observe and manipulate a part of the system so you can try and reproduce a bug easily? This is a good indicator
   that people are thinking about tests as a tool, versus a managerial requirement.
4. What is the complication load in the tests versus the code? Is the code actually testing the test instead of the other way around?
   I have one main rule, the simple tests the complicated. The filename/classname is not the main denoter.
5. Are the tests an easier place to get highly nuanced answers than the code? A good test suite helps onboard people as it 
   should allow them to play with the system as different levels clearly.

The main thing to note is that the effective approach at the start of a project, if followed blindly, will likely not stay
effective as things grow. We are trying to manage complexity with our approach so we can stay effective. That
is where we show our skill as it is a sign we know how to scale ourselves. A project 10x the size should not be 10x harder,
ideally, it should be as close to the current effort level as possible. If it is getting harder, we need to question why 
and assess our own abilities and weaknesses. 

We make decisions based on data and analysis as things change, we don't keep driving on a road that is leading to lack of 
delivery.

### Tests as documentation

Software often goes through many people's hands. Each person is deciding what is important and not important as they go 
along. Everyone has different levels of boundary spotting capability, so we are communicating what we deemed important 
and not important. This is the main purpose of the granularity and clarity that unit test level tests offer. Businesses 
don't think in negative paths, don't communicate that those paths should be dealt with. They just implicitly expect us 
to do it, and if we don't do it well, we sink our productivity with problems coming in to deal with.

This is the difference between QA level tests and our tests. QA tests deal with the obvious, our tests should deal with the 
unobvious as well. The weird implementation edge case that can only be perceived when we look at things at an atomic versus holistic level.
So it is not just about communicating what the story ticket wanted, but the journey our minds took while implementing it.
Code is our primary form of communication. The clearer communication in the code and tests, the more we can simply work without bothering
other people.

### Tests should be a continuously calculated cost

The said fact is that if we don't manage ourselves effectively, then people with less experience than us will start trying 
to organise us to be more effective. This does not end well. For example, adding more people to a team just leads to the bad 
practices increasing the problem until those people are just negated. 

So you should always keep an eye on how long the test takes to write versus the code. We are looking for long-term fastish but 
stable, versus fast now and it turning into another boat anchor in the week, which is what commonly happens. 
Test organisation is a major skill in itself and relies on an understanding of software ownership. We should leave what we hope to 
inherit.

These practices are not there to make the boss man happy, they are there so that we can play 'nicely' and efficiently together.
A team that does not monitor the efficiency of their practices leads to a lot of unneeded stress when deadlines come in. This can
lead to burn out. It is up to us to decide how we apply effort, and if we are nice we take into account how our decisions affect
others, including those who support said product.