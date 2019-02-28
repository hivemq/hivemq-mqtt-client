require "jekyll"
require "erb"

module Jekyll
  module Tabs
    class TabsBlock < Liquid::Block

      def initialize(tag_name, text, tokens)
        super
        @group = text.strip
      end

      def render(context)
        environment = context.environments.first
        environment['tabs'] = {} # reset each time
        super

        template = ERB.new <<-EOF
<ul class="tab" data-tab-group="<%= @group %>">
<% environment['tabs'].each_with_index do |(key, _), index| %>
	<li<%= index == 0 ? ' class="tab-active"' : ''%>><a href="#"><%= key %></a></li>
<% end %>
</ul>
<ul class="tab-content" data-tab-group="<%= @group %>">
<% environment['tabs'].each_with_index do |(_, value), index| %>
	<li<%= index == 0 ? ' class="tab-active"' : ''%>><%= value %></li>
<% end %>
</ul>
EOF
        template.result(binding)
      end

      Liquid::Template.register_tag('tabs', self)
    end

    class TabBlock < Liquid::Block
      alias_method :render_block, :render

      def initialize(tag_name, text, tokens)
        super
        if text == ""
          raise SyntaxError.new("No toggle name given in #{tag_name} tag")
        end
        @toggle = text.strip
      end

      def render(context)
        site = context.registers[:site]
        converter = site.find_converter_instance(::Jekyll::Converters::Markdown)
        environment = context.environments.first
        environment['tabs'] ||= {}
        environment['tabs'][@toggle] = converter.convert(render_block(context))
      end

      Liquid::Template.register_tag('tab', self)
    end
  end
end