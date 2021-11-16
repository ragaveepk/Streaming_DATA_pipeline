# Streaming-Data-Pipeline with Cloud Computing

This is the repository for CS441 final project.

| S.No. | First Name | Last Name | UIN | Email |
| :---: | :---: | :---: | :---: | :---: |
|1 | Ragavee | Poosappagounder Kandavel | 660425677 | rpoosa2@uic.edu |
|2 | Smrithi | Balki | 668488598 | sbalki3@uic.edu |
|3 | Ramiya Shree | Seshiah | 660418618 | rsesha3@uic.edu |
|4 | Anandavignesh | Velangadu Sivakumar | 662139789 | avelan2@uic.edu |
|5 | Lakshmanan | Meiyappan | fill | lmeiya2@uic.edu |

## Introduction:
The goal of this course project is to gain experience with creating a streaming data pipeline with cloud computing technologies by designing and implementing an actor-model service using [Akka](https://akka.io/) that ingests logfile generated data in real time and delivers it via an event-based service called [Kafka](https://kafka.apache.org/) to [Spark](https://spark.apache.org/) for further processing. 
Grade: 20%

## Outline:

* The goal is to create an algorithm for notifying stakeholders via email in real time when more than one ERROR or WARN messages appear within a certain length time window.
* The starting point of the pipeline is the set of instances of the logfile generator that produce log messages in real time. 
* Next in the pipeline you will create actors using Akka that reactively monitor the log files and determine if a sequence of WARN and ERROR type log messages appear within some predefined time window. 
* This information will be passed as events to Kafka that will notify the next actor in the pipeline with the information about the sequence of messages. 
* This actor will extract the messages and pass them via Kafka to your Spark program for some aggregation that you can define as part of your project, e.g., to produce a report or to run some machine learning algorithm to extract some pattern from this data - at this point it is not important what you will do with the data.
* The results must be emailed to stakeholders automatically and optionally they can be stored in a file or you can use some NoSQL database like Cassandra that you can obtain from a Docker repo. 

## Functionality:

A good starting point is to view a [general overview video made by Lightbend Corp. that describes how to build streaming pipelines using Akka and Spark with Cloudflow](https://www.youtube.com/watch?v=MaXCx0fy0xU). Your course project consists of three pipelined parts: first, create a actor system that is integrated with an event delivery system to enable notifications of real-time events, e.g., updates to log file; second, create a delivery mechanism of the obtained events of interest to Spark for data aggregation to create a summary of events; and third to deliver this summary to stakeholders via some basic email notification mechanism. You are free to determine how your pipelined computing nodes work.

You will deploy multiple instance of the log file generation program on EC2 and configure them to run for some period of time producing and storing log messages into log file in some storage. If you need to modify the generator for this purpose please go ahead and fork the repo and make appropriate changes. 

The starting point is to follow a guide on [creating real-time file monitoring services using Java NIO](https://dzone.com/articles/listening-to-fileevents-with-java-nio). Once you follow the steps of the tutorial, you will be able to create a program in Scala that creates events in response to changes in the watched files in your filesystem.

Next, you will learn how to create an [Akka-based actor](https://doc.akka.io/docs/akka/current/index.html?language=scala) program. As my students you have the subscription to the [**premium content** of Lightbend Academy](https://academy.lightbend.com/courses?search_query=FILTER_TYPE_PREMIUM) that I negotiated with the company leadership. Please make sure to use your UIC.EDU email when you [register with Lightbend Academy](https://www.lightbend.com/account/register).

After that you will learn about [Kafka](https://kafka.apache.org/quickstart) and determine how to use it to [create streams of events](https://www.youtube.com/watch?v=MYTFPTtOoLs). Your head will spin when you realize how many technology solutions are there to meet different needs of creating and deploying distributed objects in the cloud settings. Unfortunately, the time is limited and the project submission deadline is only one month away as of November 5, so I suggest you go with the baseline option, plan and distribute the work among all team members and make sure that you coordinate how to seamlessly integrate different nodes into the main project pipeline for delivery.

The penultimate node in the pipeline is your Spark-based aggregation program that obtains the information about log messages and aggregates them to deliver the aggregated information to stakeholders via email. This is a common notification style for information stakeholders in the enterprise environment that some failures happen in the deployed systems. You can use [AWS email service](https://aws.amazon.com/ses/) or some other messaging alternatives. Soon I will give a lecture on Spark so that it would nicely into your pipelined work on this course project.

Next, after creating and testing your programs locally, you will deploy it and run it on the AWS. You will produce a short movie that documents all steps of the deployment and execution of your program with your narration and you will upload this movie to [youtube](http://www.youtube.com) and as before you will submit a link to your movie as part of your submission in the README.md file. To produce a movie, you may use an academic version of [Camtasia](https://www.techsmith.com/video-editor.html) or some other cheap/free screen capture technology from the UIC webstore or an application for a movie capture of your choice. The captured web browser content should show your login name in the upper right corner of the AWS application and you should introduce all team members in the beginning of the movie speaking into the camera.

## Baseline Submission

Your baseline project submission should include your implementation, a conceptual explanation in the document or in the comments in the source code of how your algorithm and its implementation work to solve the problem, and the documentation that describe the build and runtime process, to be considered for grading. Your project submission should include all your source code as well as non-code artifacts (e.g., configuration files), your project should be buildable using the SBT, and your documentation must specify what input/outputs are.
