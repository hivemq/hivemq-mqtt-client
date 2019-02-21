# News

{% for post in site.posts %}
- [{{ post.title }} ({{ post.date | date_to_string }})]({{ post.url | absolute_url }})
{% endfor %}